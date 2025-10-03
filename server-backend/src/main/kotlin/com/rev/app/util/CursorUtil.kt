package com.rev.app.util

import java.time.Instant
import java.util.Base64

data class Cursor(val createdAt: Instant, val id: Long)

object CursorUtil {
    fun encode(createdAt: Instant, id: Long): String {
        val raw = "${'$'}{createdAt.toEpochMilli()},${'$'}id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())
    }
    fun decode(cursor: String): Cursor? = try {
        val raw = String(Base64.getUrlDecoder().decode(cursor))
        val parts = raw.split(",")
        Cursor(Instant.ofEpochMilli(parts[0].toLong()), parts[1].toLong())
    } catch (e: Exception) { null }
}
