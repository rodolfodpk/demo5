package com.example1.projections

import io.github.crabzilla.pgc.integration.EventsProjector
import io.github.crabzilla.pgc.integration.EventsProjectorProvider

class UsersProjectorFactory : EventsProjectorProvider {
  override fun create(): EventsProjector {
    return UsersEventsProjector
  }
}
