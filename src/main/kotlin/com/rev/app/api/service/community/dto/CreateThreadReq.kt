package com.rev.app.api.service.community.dto

import java.util.UUID

/**
 * 스레드 생성 요청 DTO (컨트롤러/테스트가 쓰는 이름 유지)
 * - boardId 는 경로 변수로 받으므로 여기에 두지 않습니다.
 */
data class CreateThreadReq(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val parentId: UUID? = null,
    val isPrivate: Boolean = false,
    val categoryId: UUID? = null
)
