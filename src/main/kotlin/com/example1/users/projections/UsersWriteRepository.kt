package com.example1.users.projections

import io.vertx.core.Future
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple
import java.util.UUID

object UsersWriteRepository {

    fun upsert(conn: SqlConnection, id: UUID, name: String, email: String, password: String): Future<Void> {
        return conn.preparedQuery("INSERT INTO users_view (id, name, email, password, is_active) " +
                "VALUES ($1, $2, $3, $4, $5) ")
            .execute(Tuple.of(id, name, email, password, false))
            .mapEmpty()
    }

    fun updateStatus(conn: SqlConnection, id: UUID, isActive: Boolean): Future<Void> {
        return conn.preparedQuery("UPDATE users_view set is_active = $2 where id = $1")
            .execute(Tuple.of(id, isActive))
            .mapEmpty()
    }

}