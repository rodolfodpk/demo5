package com.example1

import com.example1.projections.UsersEventsProjector
import io.github.crabzilla.pgc.api.PgcClient
import io.github.crabzilla.pgc.api.PgcCommandClient
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
    fun pgcClient(vertx: Vertx, connectOptions: PgConnectOptions, poolOptions: PoolOptions): PgcClient {
        return PgcClient.create(vertx, userJson, connectOptions, poolOptions)
    }

    @Singleton
    fun pgPool(pgcClient: PgcClient): PgPool {
        return pgcClient.pgPool
    }

    @Singleton
    @Named("sync")
    fun cmdControllerSync(pgcClient: PgcClient): CommandController<User, UserCommand, UserEvent> {
        val c = PgcCommandClient(pgcClient)
        return c.create(userConfig, false, UsersEventsProjector)
    }

    @Singleton
    @Named("async")
    fun cmdControllerAsync(pgcClient: PgcClient): CommandController<User, UserCommand, UserEvent> {
        val c = PgcCommandClient(pgcClient)
        return c.create(userConfig, false)
    }
}
