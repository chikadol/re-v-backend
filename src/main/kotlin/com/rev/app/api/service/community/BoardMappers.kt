package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes



import com.rev.app.domain.community.Board
fun Board.toBoardRes(): BoardRes =
    BoardRes(
        id = requireNotNull(id),
        createdAt = this.createdAt,
        updatedAt = null
    )

