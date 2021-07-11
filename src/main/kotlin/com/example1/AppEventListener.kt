package com.example1

import com.example1.infra.PgPoolFactory
import io.github.crabzilla.stack.deployVerticles
import io.micronaut.context.annotation.Context
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Context
class AppEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventListener::class.java)
    }

    @Inject
    lateinit var vertx: Vertx

    @Inject
    lateinit var dbConfig: PgPoolFactory.WriteDbConfig

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        val singletonVerticles = listOf<String>(
//            "service:demo5.UsersProjector",
//            "service:demo5.UsersPublisher"
        )
        val verticles = listOf(
            "service:demo5.UsersCommandVerticle"
        )
        val config = JsonObject().put("db-config", options())
        val deploymentOptions = DeploymentOptions().setConfig(config)
        vertx.deployVerticles(verticles, deploymentOptions)
            .compose { vertx.deployVerticles(singletonVerticles, deploymentOptions) }
            .onFailure { log.error(it.message, it) }
            .onSuccess { log.info("Ok") }
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
        // TODO pgPool.close
    }

    private fun options(): JsonObject {
        return JsonObject()
            .put("port", dbConfig.port)
            .put("host", dbConfig.host)
            .put("database", dbConfig.database)
            .put("user", dbConfig.user)
            .put("password", dbConfig.password)
    }

}