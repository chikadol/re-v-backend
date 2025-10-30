package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.domain.community.Board

fun Board.toRes(): BoardRes =
    BoardRes(
        id = requireNotNull(this.id) { "Board.id is null before persistence" },
        createdAt = this.createdAt
    )
