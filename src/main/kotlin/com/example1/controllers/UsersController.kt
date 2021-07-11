package com.example1.controllers

import com.example1.User
import com.example1.UserAlreadyExists
import com.example1.UserCommand
import com.example1.UserCommand.RegisterUser
import com.example1.UserEvent
import com.example1.UsersCommandVerticle
import com.example1.userJson
import com.github.f4b6a3.uuid.UuidCreator
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.stack.CommandId
import io.github.crabzilla.stack.DomainStateId
import io.github.crabzilla.stack.command.CommandController
import io.github.crabzilla.stack.command.CommandException
import io.github.crabzilla.stack.command.CommandMetadata
import io.micronaut.context.annotation.Context
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.exceptions.HttpStatusException
import io.reactivex.Single
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size


@Controller("/api/v1/users")
@Context
open class UsersController(
    val eventBus: EventBus,
    @Named("async") private val controller: CommandController<User, UserCommand, UserEvent>,
) {

    companion object {
        private val log = LoggerFactory.getLogger(UsersController::class.java)
    }

    @Status(HttpStatus.CREATED)
    @Post
    open fun post(@Valid @Body request: CreateUserRequest): Single<HttpResponse<String>> {
        val newId = UuidCreator.getTimeOrdered()
        val metadata = CommandMetadata(DomainStateId(newId), CommandId(UuidCreator.getTimeOrdered()))
        val command = RegisterUser(newId, request.name, request.email, request.password)
        return Single.create { emitter ->

//            val msg1 = JsonObject()
//                .put("metadata", metadata.toJson())
//                .put("command", JsonObject(command.toJson(userJson)))
//            eventBus.request<Boolean>(UsersCommandVerticle.ENDPOINT, msg1)
//                .onSuccess {
//                    emitter.onSuccess(HttpResponse.noContent())
//                }
//                .onFailure { error ->
//                    log.error("Error", error)
//                    emitter.onError(HttpStatusException(HttpStatus.BAD_REQUEST, error.message))
//                    }
//                }

            controller.handle(metadata, command)
                .onSuccess { session: StatefulSession<User, UserEvent> ->
                    if (log.isDebugEnabled) log.debug("Result: ${session.toSessionData()}")
                    emitter.onSuccess(HttpResponse.noContent())
                }
                .onFailure { error ->
                    val result = when (error) {
                        is UserAlreadyExists ->
                            throw HttpStatusException(HttpStatus.CONFLICT, error.message)
                        is CommandException.OptimisticLockingException ->
                            throw HttpStatusException(HttpStatus.CONFLICT, error.message)
                        is CommandException.ValidationException ->
                            throw HttpStatusException(HttpStatus.BAD_REQUEST, error.message)
                        else -> error
                    }
                    log.error("Error", result)
                    emitter.onError(result)
                }
        }

    }

}

@Introspected
data class CreateUserRequest(

    @field:[NotEmpty Size(max = 255)]
    val name: String = "",

    @field:[NotEmpty Email Size(max = 255)]
    val email: String = "",

    @field:[NotEmpty Size(max = 10)]
    val password: String = "",

    )