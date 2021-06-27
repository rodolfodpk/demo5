package com.example1.projections

import com.example1.UserEvent
import io.github.crabzilla.pgc.PgcEventsProjector
import io.github.crabzilla.stack.EventMetadata
import io.vertx.core.Future
import io.vertx.sqlclient.SqlConnection

object UsersEventsProjector: PgcEventsProjector<UserEvent> {

    override fun project(conn: SqlConnection, event: UserEvent, eventMetadata: EventMetadata): Future<Void> {
        val id = eventMetadata.aggregateRootId.id
        return when (event) {
            is UserEvent.UserRegistered ->
                UsersWriteRepository.upsert(conn, id, event.name, event.email, event.password)
            is UserEvent.UserActivated ->
                UsersWriteRepository.updateStatus(conn, id, true)
            is UserEvent.UserDeactivated ->
                UsersWriteRepository.updateStatus(conn, id, false)
        }
    }

}