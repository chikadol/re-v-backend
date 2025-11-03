package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.domain.community.Board

fun Board.toRes(): BoardRes =
    BoardRes(
        id = requireNotNull(id),
        slug = slug,
        name = name,
        description = null       // 엔티티에 없으므로 일단 null
    )
