package com.example1

import com.example1.users.userJson
import io.github.crabzilla.pgc.JsonContext
import io.github.crabzilla.pgc.JsonContextProvider
import kotlinx.serialization.json.Json

class AppJsonContextFactory : JsonContextProvider {
  override fun create(): JsonContext {
    return Demo5JsonContext()
  }
  class Demo5JsonContext : JsonContext {
    override fun json(): Json {
      return userJson
    }
  }
}
