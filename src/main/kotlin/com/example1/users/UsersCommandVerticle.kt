package com.example1.users

import io.github.crabzilla.core.Command
import io.github.crabzilla.core.CommandControllerConfig
import io.github.crabzilla.pgc.PgcAbstractVerticle
import io.github.crabzilla.pgc.command.PgcEventStore
import io.github.crabzilla.pgc.command.PgcSnapshotRepo
import io.github.crabzilla.stack.command.CommandController
import io.github.crabzilla.stack.command.CommandMetadata
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

// TODO a singleton command verticle could skip the optimistic locking but does it really worth?
class UsersCommandVerticle : PgcAbstractVerticle() {

  companion object {
    private val log = LoggerFactory.getLogger(UsersCommandVerticle::class.java)
    const val ENDPOINT = "user.command.handler"
  }

  override fun start() {

    val pgPool = pgPool(config())
    val sqlClient = sqlClient(config())

    val userConfig = CommandControllerConfig(
      "User",
      userEventHandler,
      { UserCommandHandler() } ,
      userCmdValidator
    )

    val snapshotRepo = PgcSnapshotRepo<User>(sqlClient, userJson)
    val eventStore = PgcEventStore(userConfig, pgPool, userJson,
      saveCommandOption = false,
      optimisticLockOption = false,
      eventsProjector = null)
    val controller = CommandController(userConfig, snapshotRepo, eventStore)

    vertx.eventBus().consumer<JsonObject>(ENDPOINT) { msg ->
      val metadata = CommandMetadata.fromJson(msg.body().getJsonObject("metadata"))
      val command = Command.fromJson<UserCommand>(userJson, msg.body().getJsonObject("command").toString())
      controller.handle(metadata, command)
        .onFailure { msg.fail(500, it.message) }
        .onSuccess {
          // log.info("Ok")
          msg.reply(true)
        }
    }

    log.info("Successfully started")

  }
}
