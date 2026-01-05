package com.rev.app.api.service.community.dto

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

/**
 * 게시글 상세 정보 응답 DTO
 * Redis 캐싱을 위한 Jackson 타입 정보 포함
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
data class ThreadDetailRes(
    val thread: ThreadRes,
    val commentCount: Long,
    val bookmarkCount: Long,
    val reactions: Map<String, Long>,
    val myReaction: String?,
    val bookmarked: Boolean
)