package com.example1.controllers

import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.example1.infra.CassandraConfig
import io.micronaut.context.annotation.Context
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.exceptions.HttpStatusException
import io.reactivex.Single
import io.vertx.cassandra.CassandraClient
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Controller("/api/v1/auth")
@Context
open class AuthController(@Named("scylla") private val dao: UserReadDao, private val jwt: JWTAuth) {

    companion object {
        private val log = LoggerFactory.getLogger(AuthController::class.java)
    }

    @Status(HttpStatus.OK)
    @Post("/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun authenticateSingle(email: String, password: String): Single<HttpResponse<String>> {
        return Single.create { emitter ->
            dao.select(email, password)
                .onFailure {
                    log.error("Error", it)
                    emitter.onError(it) }
                .onSuccess { found ->
                    log.info("Found: {}", found)
                    if (found) {
                        val principal = JsonObject().put("user", email)
                        val jwtOptions = JWTOptions().setAudience(listOf("")).setExpiresInMinutes(60)
                        val token = jwt.generateToken(principal, jwtOptions)
                        val response = HttpResponse.noContent<String>().header("JWT-Assertion", token)
                        emitter.onSuccess(response)
                    } else {
                        emitter.onError(HttpStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"))
                    }
                }
        }
    }

}

interface UserReadDao {
    fun select(email: String, password: String): Future<Boolean>
}

@Singleton
@Named("scylla")
class ScyllaUserReadDao(private val cassandra: CassandraClient, config: CassandraConfig) : UserReadDao {
    private val select = "SELECT password FROM ${config.keyspace}.users_view WHERE email = ?"
    override fun select(email: String, password: String): Future<Boolean> {
        return cassandra.prepare(select)
            .compose { ps: PreparedStatement -> cassandra.execute(ps.bind(email)) }
            .map { rs ->
                rs.one()?.getString("password") ?:
                        rs.one()?.getString("password") == password}
    }
}

@Singleton
@Named("postgres")
class PostgressUserReadDao(private val pool: PgPool) : UserReadDao {
    override fun select(email: String, password: String): Future<Boolean> {
        return pool.preparedQuery("SELECT password FROM users_view WHERE email = $1")
            .execute(Tuple.of(email))
            .map { rs -> if (rs.size() == 0) false else rs.first().getString("password") == password }
    }
}
