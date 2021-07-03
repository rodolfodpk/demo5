package com.example1

import io.github.crabzilla.pgc.integration.JsonContext
import io.github.crabzilla.pgc.integration.JsonContextProvider
import kotlinx.serialization.json.Json

class AppJsonFactory : JsonContextProvider {
  override fun create(): JsonContext {
    return DefaultJsonContext()
  }
  class DefaultJsonContext : JsonContext {
    override fun json(): Json {
      return userJson
    }
  }
}
