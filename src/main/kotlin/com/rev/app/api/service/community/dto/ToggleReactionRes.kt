package com.rev.app.api.service.community.dto
data class ToggleReactionRes(
    val toggled: Boolean,                // true=추가, false=삭제
    val counts: Map<String, Long>        // 타입별 카운트
)
