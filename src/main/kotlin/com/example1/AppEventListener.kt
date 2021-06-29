package com.example1

import com.example1.infra.registerLocalCodec
import com.example1.projections.UsersEventsProjector
import io.github.crabzilla.pgc.api.PgcClient
import io.github.crabzilla.pgc.api.PgcVerticlesClient
import io.github.crabzilla.stack.EventsPublisherOptions
import io.micronaut.context.annotation.Context
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Context
class AppEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventListener::class.java)
    }

    @Inject
    lateinit var pgcClient: PgcClient

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        pgcClient.vertx.registerLocalCodec()
        val c = PgcVerticlesClient(pgcClient)
        val options1 = EventsPublisherOptions.Builder()
            .targetEndpoint("users")
            .build()
        c.addEventsPublisher("users", options1)
        c.addEventsProjector("users", UsersEventsProjector)
//        val options2 = EventsPublisherOptions.Builder()
//            .targetEndpoint(NatsProjectorVerticle.ENDPOINT)
//            .build()
//        c.addEventsPublisher("nats", options2)
//        c.deployVerticles()
//            .onSuccess { log.info("Success") }
//            .onFailure { log.error(it.message, it) }
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        // TODO pgcClient.close()
    }

}