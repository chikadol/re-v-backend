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
        val p = raw.split(",")
        Cursor(Instant.ofEpochMilli(p[0].toLong()), p[1].toLong())
    } catch (e: Exception) { null }
}
