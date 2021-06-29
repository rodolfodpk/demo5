package com.example1.projections


import io.nats.streaming.StreamingConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Publishes domain events to NATS (single writer process)
 */
@Singleton
@Named("nats")
class NatsProjectorVerticle(private val nats: StreamingConnection) : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(NatsProjectorVerticle::class.java)
        const val ENDPOINT = "nats.projection"
    }

    override fun start() {
        vertx.eventBus()
            .consumer<JsonObject>(ENDPOINT) { msg ->
                val asJson = msg.body()
                // TODO use cache for idempotency val eventId = asJson.getLong("eventId")
                val eventAsJson = asJson.getJsonObject("eventAsjJson")
                project(eventAsJson)
                    .onFailure { msg.fail(500, it.message) }
                    .onSuccess { msg.reply(true)}
            }
        log.info("Started on endpoint [$ENDPOINT]")
    }

    private fun project(eventAsJson: JsonObject): Future<Void> {
        return vertx.executeBlocking<Void> { promise -> try {
            nats.publish(ENDPOINT, eventAsJson.toBuffer().bytes)
            // log.info("Published {} to {}", eventAsJson, targetTopic)
            promise.complete()
        } catch (e: Exception) {
            promise.fail(e)
        }
        }.mapEmpty()
    }

}

