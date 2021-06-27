package com.example1.projections

import com.example1.UserEvent
import io.github.crabzilla.core.DomainEvent
import io.github.crabzilla.pgc.PgcClient
import io.github.crabzilla.stack.EventRecord
import io.micronaut.context.annotation.Context
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

@Context
class UserProjectorVerticle(private val pgcClient: PgcClient) : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(UserProjectorVerticle::class.java)
        const val ENDPOINT = "user.projection"
    }

    override fun start() {
        vertx.eventBus().consumer<JsonObject>(ENDPOINT) { msg ->
            val eventRecord = EventRecord.fromJsonObject(msg.body())
            pgcClient.pgPool.withConnection { conn ->
                val event = DomainEvent.fromJson<UserEvent>(pgcClient.json, eventRecord.eventAsjJson.toString())
                UsersEventsProjector.project(conn, event, eventRecord.eventMetadata)
            }
            .onFailure {
                log.error(it.message, it)
                msg.fail(500, it.message)
            }
            .onSuccess {
                msg.reply(true)
            }
        }
        log.info("Started consuming from topic [$ENDPOINT]")
    }

    override fun stop() {
        log.info("Stopped")
    }

}