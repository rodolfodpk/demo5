package com.example1.projections

import com.example1.UserEvent
import io.github.crabzilla.core.DomainEvent
import io.github.crabzilla.pgc.PgcEventsProjectorApi
import io.github.crabzilla.stack.EventMetadata
import io.vertx.core.Future
import io.vertx.sqlclient.SqlConnection

object UsersEventsProjector: PgcEventsProjectorApi {

    override fun project(conn: SqlConnection, event: DomainEvent, eventMetadata: EventMetadata): Future<Void> {
        val id = eventMetadata.aggregateRootId.id
        return when (val e = event as UserEvent) {
            is UserEvent.UserRegistered ->
                UsersWriteRepository.upsert(conn, id, e.name, e.email, e.password)
            is UserEvent.UserActivated ->
                UsersWriteRepository.updateStatus(conn, id, true)
            is UserEvent.UserDeactivated ->
                UsersWriteRepository.updateStatus(conn, id, false)
        }
    }

}