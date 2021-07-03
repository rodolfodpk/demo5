package com.example1.projections

import io.github.crabzilla.pgc.PgcEventsProjectorApi
import io.github.crabzilla.pgc.PgcEventsProjectorProvider

class UsersProjectorFactory : PgcEventsProjectorProvider {
  override fun create(): PgcEventsProjectorApi {
    return UsersEventsProjector
  }
}
