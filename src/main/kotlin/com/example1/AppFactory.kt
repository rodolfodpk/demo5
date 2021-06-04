package com.example1

import com.example1.user.User
import com.example1.user.UserCommand
import com.example1.user.UserEvent
import com.example1.user.userConfig
import io.github.crabzilla.pgc.CommandControllerFactory
import io.github.crabzilla.pgc.EventsPublisherVerticleFactory
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.EventsPublisherVerticle
import io.github.crabzilla.stack.EventsPublisherVerticleOptions
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.pgclient.PgPool
import javax.inject.Named
import javax.inject.Singleton

@Factory
private class AppFactory {

    @Singleton
    fun jwt(vertx: Vertx, @Value("\${token.hmac.secret}") secret: String): JWTAuth {
        val opt = PubSecKeyOptions()
            .setAlgorithm("HS256")
            .setBuffer(secret)
        return JWTAuth.create(vertx, JWTAuthOptions().addPubSecKey(opt))
    }

    @Bean
    @Singleton
    @Named("postgress")
    fun pgUserCommandController(@Named("pgPool") pgPool: PgPool):
            CommandController<User, UserCommand, UserEvent> {
        return CommandControllerFactory.create(userConfig, pgPool)
    }

    @Singleton
    @Named("users")
    fun publisherVerticle1(eventBus: EventBus, @Named("pgPool") pgPool: PgPool): EventsPublisherVerticle {
        val options = EventsPublisherVerticleOptions.Builder()
            .targetEndpoint(UserProjectorVerticle.ENDPOINT)
            .eventBus(eventBus)
            .build()
        return EventsPublisherVerticleFactory.create("users", pgPool, options)
    }

    @Singleton
    @Named("nats")
    fun publisherVerticle2(eventBus: EventBus, @Named("pgPool") pgPool: PgPool): EventsPublisherVerticle {
        val options = EventsPublisherVerticleOptions.Builder()
            .targetEndpoint(NatsProjectorVerticle.ENDPOINT)
            .eventBus(eventBus)
            .build()
        return EventsPublisherVerticleFactory.create("nats", pgPool, options)
    }

}
