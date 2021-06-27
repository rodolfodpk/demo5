package com.example1

import com.example1.projections.NatsProjectorVerticle
import com.example1.projections.UserProjectorVerticle
import com.example1.projections.UsersEventsProjector
import io.github.crabzilla.pgc.PgcClient
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.EventsPublisherOptions
import io.github.crabzilla.stack.EventsPublisherVerticle
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.vertx.core.Vertx
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

    @Singleton
    fun pgcClient(vertx: Vertx, @Named("pgPool") pgPool: PgPool): PgcClient {
        return PgcClient(vertx, pgPool, userJson)
    }

    @Singleton
    fun c1(pgcClient: PgcClient): CommandController<User, UserCommand, UserEvent> {
        return pgcClient.create(userConfig, false, UsersEventsProjector)
    }

    @Singleton
    @Named("users")
    fun publisherVerticle1(pgcClient: PgcClient): EventsPublisherVerticle {
        val options = EventsPublisherOptions.Builder()
            .targetEndpoint(UserProjectorVerticle.ENDPOINT)
            .build()
        return pgcClient.create("users", options)
    }

    @Singleton
    @Named("nats")
    fun publisherVerticle2(pgcClient: PgcClient): EventsPublisherVerticle {
        val options = EventsPublisherOptions.Builder()
            .targetEndpoint(NatsProjectorVerticle.ENDPOINT)
            .build()
        return pgcClient.create("nats", options)
    }

}
