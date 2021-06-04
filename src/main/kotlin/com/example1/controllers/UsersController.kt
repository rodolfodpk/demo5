package com.example1.controllers

import com.example1.user.User
import com.example1.user.UserAlreadyExists
import com.example1.user.UserCommand
import com.example1.user.UserCommand.RegisterUser
import com.example1.user.UserEvent
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.stack.AggregateRootId
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.CommandException
import io.github.crabzilla.stack.CommandMetadata
import io.micronaut.context.annotation.Context
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.exceptions.HttpStatusException
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Controller("/api/v1/users")
@Context
open class UsersController(private val controller: CommandController<User, UserCommand, UserEvent>) {

    companion object {
        private val log = LoggerFactory.getLogger(UsersController::class.java)
    }

    @Status(HttpStatus.CREATED)
    @Post
    open fun post(@Valid @Body request: CreateUserRequest): Single<StatefulSession.SessionData> {
        val newId = UUID.randomUUID()
        if (log.isDebugEnabled) log.debug("*** Will generate a new command $newId")
        val metadata = CommandMetadata(AggregateRootId(newId))
        val command = RegisterUser(newId, request.name, request.email, request.password)
        return Single.create { emitter ->
            controller.handle(metadata, command)
                .onFailure { error ->
                    val result = when (error) {
                        is UserAlreadyExists ->
                            throw HttpStatusException(HttpStatus.CONFLICT, error.message)
                        is CommandException.WriteConcurrencyException ->
                            throw HttpStatusException(HttpStatus.CONFLICT, error.message)
                        is CommandException.ValidationException ->
                            throw HttpStatusException(HttpStatus.BAD_REQUEST, error.message)
                        else -> error
                    }
                    log.error("Error", result)
                    emitter.onError(result)
                }
                .onSuccess { session: StatefulSession<User, UserEvent> ->
                    if (log.isDebugEnabled) log.debug("Result: ${session.toSessionData()}")
                    emitter.onSuccess(session.toSessionData())
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
    val password: String = ""

)