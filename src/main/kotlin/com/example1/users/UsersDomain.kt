package com.example1.users

import com.example1.users.UserCommand.ActivateUser
import com.example1.users.UserCommand.DeactivateUser
import com.example1.users.UserCommand.RegisterUser
import com.example1.users.UserEvent.UserActivated
import com.example1.users.UserEvent.UserDeactivated
import com.example1.users.UserEvent.UserRegistered
import io.github.crabzilla.core.Command
import io.github.crabzilla.core.CommandHandler
import io.github.crabzilla.core.CommandHandlerApi.ConstructorResult
import io.github.crabzilla.core.CommandValidator
import io.github.crabzilla.core.DomainEvent
import io.github.crabzilla.core.DomainState
import io.github.crabzilla.core.EventHandler
import io.github.crabzilla.core.Snapshot
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.core.javaModule
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.UUID

/**
 * User events
 */
@Serializable
sealed class UserEvent : DomainEvent() {
  @Serializable
  @SerialName("UserRegistered")
  data class UserRegistered(@Contextual val id: UUID, val name: String, val email: String, val password: String)
    : UserEvent()

  @Serializable
  @SerialName("UserActivated")
  data class UserActivated(val reason: String) : UserEvent()

  @Serializable
  @SerialName("UserDeactivated")
  data class UserDeactivated(val reason: String) : UserEvent()
}

/**
 * User commands
 */
@Serializable
sealed class UserCommand : Command() {
  @Serializable
  @SerialName("RegisterUser")
  data class RegisterUser(@Contextual val userId: UUID, val name: String, val email: String, val password: String)
    : UserCommand()

  @Serializable
  @SerialName("ActivateUser")
  data class ActivateUser(val reason: String) : UserCommand()

  @Serializable
  @SerialName("DeactivateUser")
  data class DeactivateUser(val reason: String) : UserCommand()
}

/**
 * User aggregate root
 */
@Serializable
@SerialName("User")
data class User(@Contextual val id: UUID, val name: String, val email: String, val password: String,
                val isActive: Boolean = true, val reason: String? = null) : DomainState() {

  companion object {
    fun create(id: UUID, name: String, email: String, password: String): ConstructorResult<User, UserEvent> {
      return ConstructorResult(state = User(id = id, name = name, email = email, password = password),
        events = arrayOf(UserRegistered(id = id, name = name, email = email,
          password = password)))
    }
  }

  fun activate(reason: String): List<UserEvent> {
    return listOf(UserActivated(reason))
  }

  fun deactivate(reason: String): List<UserEvent> {
    return listOf(UserDeactivated(reason))
  }
}

/**
 * A command validator. You could use https://github.com/konform-kt/konform
 */
val userCmdValidator = CommandValidator<UserCommand> { command ->
  when (command) {
    is RegisterUser -> listOf()
    is ActivateUser -> listOf()
    is DeactivateUser -> listOf()
  }
}

/**
 * This function will apply an event to user state
 */
val userEventHandler = EventHandler<User, UserEvent> { state, event ->
  when (event) {
    is UserRegistered -> User.create(id = event.id, name = event.name, email = event.email, password = event.password).state
    is UserActivated -> state!!.copy(isActive = true, reason = event.reason)
    is UserDeactivated -> state!!.copy(isActive = false, reason = event.reason)
  }
}

/**
 * User errors
 */
class UserAlreadyExists(id: UUID) : IllegalStateException("User $id already exists")

/**
 * User command handler
 */
class UserCommandHandler : CommandHandler<User, UserCommand, UserEvent> {

  override fun handleCommand(
    command: UserCommand,
    eventHandler: EventHandler<User, UserEvent>,
    snapshot: Snapshot<User>?
    )
  : StatefulSession<User, UserEvent> {

    return when (command) {

      is RegisterUser -> {
        if (snapshot == null)
          withNew(User.create(
            id = command.userId,
            name = command.name,
            email = command.email,
            password = command.password),
            userEventHandler)
        else throw UserAlreadyExists(command.userId)
      }

      is ActivateUser -> with(snapshot!!, userEventHandler).execute { it.activate(command.reason) }

      is DeactivateUser -> with(snapshot!!, userEventHandler).execute { it.deactivate(command.reason) }

    }
  }
}

/**
 * kotlinx.serialization
 */
@kotlinx.serialization.ExperimentalSerializationApi
val userModule = SerializersModule {
  include(javaModule)
  polymorphic(DomainState::class) {
    subclass(User::class, User.serializer())
  }
  polymorphic(Command::class) {
    subclass(RegisterUser::class, RegisterUser.serializer())
    subclass(ActivateUser::class, ActivateUser.serializer())
    subclass(DeactivateUser::class, DeactivateUser.serializer())
  }
  polymorphic(DomainEvent::class) {
    subclass(UserRegistered::class, UserRegistered.serializer())
    subclass(UserActivated::class, UserActivated.serializer())
    subclass(UserDeactivated::class, UserDeactivated.serializer())
  }
}

val userJson = Json { serializersModule = userModule }