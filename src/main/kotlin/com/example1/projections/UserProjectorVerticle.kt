package com.example1

import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.example1.infra.CassandraConfig
import com.example1.UserEvent.UserActivated
import com.example1.UserEvent.UserDeactivated
import com.example1.UserEvent.UserRegistered
import io.github.crabzilla.core.DomainEvent
import io.micronaut.context.annotation.Context
import io.vertx.cassandra.CassandraClient
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Named
import javax.inject.Singleton

@Context
class UserProjectorVerticle(@Named("scylla") private val repo: UserWriteDao) : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(UserProjectorVerticle::class.java)
        const val ENDPOINT = "user.projection"
    }

    override fun start() {
        vertx.eventBus()
            .consumer<JsonObject>(ENDPOINT) { msg ->
                val asJson = msg.body()
                val aggregateId = UUID.fromString(asJson.getString("aggregateId"))
                // TODO use cache for idempotency val eventId = asJson.getLong("eventId")
                val eventAsJson = asJson.getJsonObject("eventAsjJson")
                project(aggregateId, eventAsJson)
                    .onFailure {
                        log.error("ao projetar ${asJson}", it)
                        msg.fail(500, it.message) }
                    .onSuccess { msg.reply(true)}
            }
        log.info("Started on endpoint [$ENDPOINT]")
    }

    private fun project(id: UUID, eventAsJson: JsonObject): Future<Void> {
        val event = DomainEvent.fromJson<UserEvent>(userJson, eventAsJson.toString())
        // if (log.isDebugEnabled) log.debug("Will project event $event to read model")
        return when (event) {
            is UserRegistered -> repo.upsert(id, event.name, event.email, event.password)
            is UserActivated -> repo.updateStatus(id, true)
            is UserDeactivated -> repo.updateStatus(id, false)
        }
    }

    interface UserWriteDao {
        fun upsert(id: UUID, name: String, email: String, password: String): Future<Void>
        fun updateStatus(id: UUID, isActive: Boolean): Future<Void>
    }

    @Singleton
    @Named("scylla")
    class ScyllaUserWriteDao(private val cassandra: CassandraClient, config: CassandraConfig) : UserWriteDao {
        // select count(*) from identity_demo.users_view;
        private val upsert = "INSERT INTO ${config.keyspace}.users_view (id, name, email, password) values (?, ?, ?, ?)"
        private val updateStatus = "UPDATE ${config.keyspace}.users_view set is_active = ? where id = ?"
        override fun upsert(id: UUID, name: String, email: String, password: String): Future<Void> {
            val promise = Promise.promise<Void>()
            cassandra.prepare(upsert)
                .compose { ps: PreparedStatement -> cassandra.execute(ps.bind(id, name, email, password)) }
                .onSuccess { rs ->
                    if (rs.wasApplied() && rs.remaining() != 1) {
                        promise.complete()
                    } else {
                       promise.fail("not applied")
                    }
                }
                .onFailure { promise.fail(it) }
          return promise.future()
        }

        override fun updateStatus(id: UUID, isActive: Boolean): Future<Void> {
            return cassandra.prepare(updateStatus)
                .compose { ps: PreparedStatement -> cassandra.execute(ps.bind(isActive, id)) }
                .mapEmpty()
        }
    }

    @Singleton
    @Named("postgres")
    class PgClientUserWriteDao(private val pool: PgPool) : UserWriteDao {
        override fun upsert(id: UUID, name: String, email: String, password: String): Future<Void> {
            return pool.preparedQuery("INSERT INTO users_view (id, name, email, password, is_active) " +
                    "VALUES ($1, $2, $3, $4, $5) ")
                .execute(Tuple.of(id, name, email, password, false))
                .mapEmpty()
        }
        override fun updateStatus(id: UUID, isActive: Boolean): Future<Void> {
            return pool.preparedQuery("UPDATE users_view set is_active = $2 where id = $1")
                .execute(Tuple.of(id, isActive))
                .mapEmpty()
        }
    }

}