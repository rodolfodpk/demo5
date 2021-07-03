package com.example1

import com.example1.projections.UsersEventsProjector
import io.github.crabzilla.pgc.PgcCommandControllerClient
import io.github.crabzilla.pgc.PgcCommandControllerFactory
import io.github.crabzilla.stack.CommandController
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.vertx.core.Vertx
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
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
    fun pgcClient(vertx: Vertx, connectOptions: PgConnectOptions, poolOptions: PoolOptions)
    : PgcCommandControllerClient {
        return PgcCommandControllerClient.create(vertx, userJson, connectOptions, poolOptions)
    }

    @Singleton
    fun pgPool(pgcClient: PgcCommandControllerClient): PgPool {
        return pgcClient.pgPool
    }

    @Singleton
    @Named("sync")
    fun cmdControllerSync(pgcClient: PgcCommandControllerClient): CommandController<User, UserCommand, UserEvent> {
        return PgcCommandControllerFactory(pgcClient).create(userConfig, false, UsersEventsProjector)
    }

    @Singleton
    @Named("async")
    fun cmdControllerAsync(pgcClient: PgcCommandControllerClient): CommandController<User, UserCommand, UserEvent> {
        return PgcCommandControllerFactory(pgcClient).create(userConfig, false)
    }
}
