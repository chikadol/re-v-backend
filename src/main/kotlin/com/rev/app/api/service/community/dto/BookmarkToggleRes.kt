package com.rev.app.api.service.community.dto

/**
 * 북마크 토글 결과 DTO
 * - toggled: 이번 호출로 북마크가 새로 생겼는지(true) / 삭제됐는지(false)
 * - count: 현재 이 글의 북마크 총 개수
 */
data class BookmarkToggleRes(
    val toggled: Boolean,
    val count: Long
)

/**
 * 단순 북마크 개수 조회용 DTO
 */
data class BookmarkCountRes(
    val count: Long
)
