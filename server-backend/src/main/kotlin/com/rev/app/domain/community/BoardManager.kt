package com.rev.app.domain.community

import jakarta.persistence.*
import java.io.Serializable

@Entity @Table(name = "board_manager", schema = "rev")
@IdClass(BoardManagerId::class)
class BoardManager(
    @Id var boardId: Long = 0,
    @Id var userId: Long = 0
)
data class BoardManagerId(var boardId: Long = 0, var userId: Long = 0): Serializable
