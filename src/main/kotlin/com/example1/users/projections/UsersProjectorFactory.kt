package com.example1.users.projections

import io.github.crabzilla.pgc.projector.EventsProjector
import io.github.crabzilla.pgc.projector.EventsProjectorProvider

class UsersProjectorFactory : EventsProjectorProvider {
  override fun create(): EventsProjector {
    return UsersEventsProjector
  }
}