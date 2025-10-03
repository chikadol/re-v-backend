package com.rev.app.domain.community

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDate

@Entity @Table(name = "view_log_daily", schema = "rev")
@IdClass(ViewLogId::class)
class ViewLogDaily(
    @Id var date: LocalDate = LocalDate.now(),
    @Id var threadId: Long = 0,
    var views: Long = 0
)
data class ViewLogId(var date: LocalDate = LocalDate.now(), var threadId: Long = 0): Serializable
