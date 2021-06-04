package com.example1.infra

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.nats.streaming.NatsStreaming
import io.nats.streaming.Options
import io.nats.streaming.StreamingConnection
import java.util.Objects
import javax.inject.Singleton

@Factory
class NatsStreamFactory {

    // https://github.com/nats-io/stan.java#subscriber-rate-limiting
    // https://github.com/nats-io/stan.java#sharing-a-nats-connection

    @Singleton
    fun options(config: NatsStreamingConfig) : Options {
        return Options.Builder()
            .clusterId(config.clusterId)
            .clientId(config.clientId)
            .natsUrl(config.getUrl())
            .build()
    }

    @Singleton
    fun createNatConnection(config: NatsStreamingConfig, options: Options): StreamingConnection {
        return NatsStreaming.connect(config.clusterId, config.clientId, options)
    }

    @Singleton
    class NatsStreamingConfig {

        val NATS_PROTOCOL = "nats://"

        @Value("\${nats.host}")
        lateinit var host :String

        @Value("\${nats.port}")
        var port :Int = 4222

        @Value("\${nats.user}")
        lateinit var user :String

        @Value("\${nats.password}")
        lateinit var password :String

        @Value("\${nats.client-id}")
        lateinit var clientId :String

        @Value("\${nats.cluster-id}")
        lateinit var clusterId :String

        fun getUrl(): String? {
            return if (Objects.nonNull(user) || Objects.nonNull(password)) {
                "$NATS_PROTOCOL$user:$password@$host:$port"
            } else {
                "$NATS_PROTOCOL$host:$port"
            }
        }
    }

}