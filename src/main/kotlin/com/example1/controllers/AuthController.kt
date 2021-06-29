package com.example1.controllers

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
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Controller("/api/v1/auth")
@Context
open class AuthController(private val dao: UserReadDao, private val jwt: JWTAuth) {

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
                    emitter.onError(it)
                }
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

@Singleton
class UserReadDao(private val pool: PgPool) {
    fun select(email: String, password: String): Future<Boolean> {
        return pool.preparedQuery("SELECT password FROM users_view WHERE email = $1")
            .execute(Tuple.of(email))
            .map { rs -> if (rs.size() == 0) false else rs.first().getString("password") == password }
    }
}
