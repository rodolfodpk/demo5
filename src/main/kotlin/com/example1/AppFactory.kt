package com.example1

import com.example1.projections.UsersEventsProjector
import io.github.crabzilla.core.CommandControllerConfig
import io.github.crabzilla.pgc.command.CommandControllerClient
import io.github.crabzilla.stack.command.CommandController
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
    : CommandControllerClient {
        return CommandControllerClient.create(vertx, userJson, connectOptions, poolOptions)
    }

    @Singleton
    fun pgPool(pgcClient: CommandControllerClient): PgPool {
        return pgcClient.pgPool
    }

    @Singleton
    fun userConfig(vertx: Vertx): CommandControllerConfig<User, UserCommand, UserEvent> {
        return CommandControllerConfig(
            "User",
            userEventHandler,
            { UserCommandHandler() } ,
            userCmdValidator
        )
    }

    @Singleton
    @Named("sync")
    fun cmdControllerSync(pgcClient: CommandControllerClient,
                          userConfig: CommandControllerConfig<User, UserCommand, UserEvent>)
    : CommandController<User, UserCommand, UserEvent> {
        return pgcClient.create(userConfig,
            saveCommandOption = false,
            optimisticLockOption = false,
            eventsProjector = UsersEventsProjector)
    }

    @Singleton
    @Named("async")
    fun cmdControllerAsync(pgcClient: CommandControllerClient,
                           userConfig: CommandControllerConfig<User, UserCommand, UserEvent>)
    : CommandController<User, UserCommand, UserEvent> {
        return pgcClient.create(userConfig,
            saveCommandOption = false,
            optimisticLockOption = false,
            eventsProjector = null)
    }
}
