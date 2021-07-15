package com.example1

import com.github.f4b6a3.uuid.UuidCreator
import java.io.File
import java.util.UUID

class CreateUsersFile {

  companion object {

    private fun Sequence<UUID>.writeTo(file: File) {
      file.bufferedWriter().use { writer -> forEach { writer.appendLine(it.toString()) } }
    }

    @JvmStatic
    fun main(args: Array<String>) {
      val sequence = generateSequence { UuidCreator.getTimeOrdered() }
      sequence.take(215_000).writeTo(File("215k-users.txt"))
    }

  }

}