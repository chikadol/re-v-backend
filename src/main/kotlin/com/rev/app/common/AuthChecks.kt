package com.rev.app.common

import java.util.UUID

fun assertOwner(me: UUID, authorId: UUID) {
    require(me == authorId) { "권한이 없습니다." }
}