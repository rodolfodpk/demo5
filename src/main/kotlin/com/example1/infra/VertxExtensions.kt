package com.example1.infra

import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageCodec

fun <T> AsyncResult<T>.handle(promise: Promise<T>) {
    return if (this.failed()) promise.fail(this.cause()) else promise.complete(this.result())
}

fun <T> AsyncResult<T>.handleVoid(promise: Promise<Void>) {
    return if (this.failed()) promise.fail(this.cause()) else promise.complete()
}


fun Vertx.registerLocalCodec() {
    eventBus().unregisterCodec("local")
    eventBus().registerCodec(object : MessageCodec<Any, Any> {
        override fun decodeFromWire(pos: Int, buffer: Buffer?) = throw NotImplementedError()
        override fun encodeToWire(buffer: Buffer?, s: Any?) = throw NotImplementedError()
        override fun transform(s: Any?) = s
        override fun name() = "local"
        override fun systemCodecID(): Byte = -1
    })
}

fun <T> EventBus.localRequest(address: String,
                              message: Any,
                              options: DeliveryOptions? = null,
                              replyHandler: ((AsyncResult<Message<T>>) -> Unit)? = null) {
    val deliveryOptions = options?.let { DeliveryOptions(options) } ?: DeliveryOptions()
    deliveryOptions.apply {
        codecName = "local"
        isLocalOnly = true
    }
    request(address, message, deliveryOptions, replyHandler)
}

fun <T: Any> Message<T>.localReply(reply: Any, options: DeliveryOptions? = null) {
    val deliveryOptions = options?.let { DeliveryOptions(options) } ?: DeliveryOptions()
    deliveryOptions.apply {
        codecName = "local"
        isLocalOnly = true
    }
    reply(reply, deliveryOptions)
}
