package com.example1.infra

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus

@Factory
class VertxFactory {

    @Bean
    @Context
    fun vertx(): Vertx {
        return Vertx.vertx()
    }

    @Bean
    @Context
    fun eventbus(vertx: Vertx): EventBus {
        return vertx.eventBus()
    }

}