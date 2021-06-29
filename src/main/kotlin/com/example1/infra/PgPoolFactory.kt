package com.example1.infra

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.PoolOptions

@Factory
class PgPoolFactory {

    @Context
    fun poolOptions(vertx: Vertx, config: WriteDbConfig): PoolOptions {
        return PoolOptions()
            .setMaxSize(config.maxSize)
//            .setMaxWaitQueueSize(-1)
//            .setIdleTimeout(2)
//            .setIdleTimeoutUnit(TimeUnit.SECONDS)
    }

    @Context
    fun pgConnectOptions(vertx: Vertx, config: WriteDbConfig): PgConnectOptions {
        return PgConnectOptions()
            .setPort(config.port)
            .setHost(config.host)
            .setDatabase(config.database)
            .setUser(config.user)
            .setPassword(config.password)
            .setCachePreparedStatements(true)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000)
    }

    @ConfigurationProperties("vertx.pg.client")
    class WriteDbConfig  {
        @Value("\${host}")
        lateinit var host: String
        @Value("\${port}")
        var port: Int = 5432
        @Value("\${database}")
        lateinit var database: String
        @Value("\${user}")
        lateinit var user: String
        @Value("\${password}")
        lateinit var password: String
        @Value("\${maxSize}")
        var maxSize: Int = 100
    }
}
