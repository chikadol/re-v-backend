package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ApiResponse as ApiResponseDto
import com.rev.app.api.controller.dto.ThreadCreateRequest
import com.rev.app.api.controller.dto.ThreadResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadDetailRes
import com.rev.app.api.service.community.dto.ThreadRes
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
@Tag(name = "게시글", description = "게시글 관련 API")
@SecurityRequirement(name = "bearerAuth")
class ThreadController(
    private val threadService: ThreadService
) {

    private val allowedSort = setOf("createdAt")

    @Operation(
        summary = "게시글 목록 조회",
        description = "특정 게시판의 게시글 목록을 페이징하여 조회합니다.\n\n" +
            "## 파라미터\n" +
            "- `boardId`: 게시판 UUID\n" +
            "- `tags`: 태그 필터 (선택, 여러 개 가능)\n" +
            "- `search`: 검색어 (선택, 제목/내용 검색)\n" +
            "- `page`: 페이지 번호 (기본값: 0)\n" +
            "- `size`: 페이지 크기 (기본값: 20)\n" +
            "- `sort`: 정렬 기준 (예: `createdAt,desc`)\n\n" +
            "## 예제\n" +
            "- 태그 필터: `/api/threads/{boardId}/threads?tags=공연&tags=리뷰`\n" +
            "- 검색: `/api/threads/{boardId}/threads?search=아이돌`\n" +
            "- 페이징: `/api/threads/{boardId}/threads?page=0&size=10&sort=createdAt,desc`"
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = "{\n" +
                            "  \"success\": true,\n" +
                            "  \"data\": {\n" +
                            "    \"content\": [\n" +
                            "      {\n" +
                            "        \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                            "        \"boardId\": \"123e4567-e89b-12d3-a456-426614174001\",\n" +
                            "        \"title\": \"게시글 제목\",\n" +
                            "        \"content\": \"게시글 내용\",\n" +
                            "        \"authorId\": \"123e4567-e89b-12d3-a456-426614174002\",\n" +
                            "        \"createdAt\": \"2024-01-01T00:00:00Z\",\n" +
                            "        \"tags\": [\"태그1\", \"태그2\"]\n" +
                            "      }\n" +
                            "    ],\n" +
                            "    \"totalElements\": 100,\n" +
                            "    \"totalPages\": 10,\n" +
                            "    \"number\": 0,\n" +
                            "    \"size\": 20,\n" +
                            "    \"first\": true,\n" +
                            "    \"last\": false\n" +
                            "  }\n" +
                            "}"
                    )]
                )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            )
        ]
    )
    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @Parameter(description = "게시판 UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable boardId: UUID,
        @Parameter(description = "태그 필터 (여러 개 가능)", required = false)
        @RequestParam(name = "tags", required = false) tags: List<String>?,
        @Parameter(description = "검색어 (제목/내용 검색)", required = false, example = "아이돌")
        @RequestParam(name = "search", required = false) search: String?,
        @Parameter(description = "페이징 정보", required = false)
        pageable: Pageable
    ): ResponseEntity<ApiResponseDto<com.rev.app.api.controller.PageResponse<ThreadRes>>> {
        return try {
            // PageResponse를 반환하는 캐시 가능한 메서드 사용
            val pageResponse = if (!search.isNullOrBlank()) {
                threadService.searchAsPageResponse(boardId, search, pageable)
            } else {
                threadService.listPublicAsPageResponse(boardId, pageable, tags)
            }
            ResponseHelper.ok(pageResponse)
        } catch (e: Exception) {
            e.printStackTrace() // 로그 출력
            ResponseHelper.error("THREAD_LIST_FAILED", "게시글 목록을 불러오는 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @Operation(
        summary = "게시글 상세 조회",
        description = "특정 게시글의 상세 정보를 조회합니다.\n\n" +
            "## 반환 정보\n" +
            "- 게시글 기본 정보 (제목, 내용, 작성자 등)\n" +
            "- 댓글 수, 북마크 수\n" +
            "- 반응 수 (좋아요, 사랑해요)\n" +
            "- 현재 사용자의 반응 및 북마크 여부"
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "성공"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "게시글을 찾을 수 없음"
            )
        ]
    )
    @GetMapping("/detail/{threadId}")
    fun getDetail(
        @Parameter(description = "게시글 UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable threadId: UUID,
        @Parameter(hidden = true)
        @AuthenticationPrincipal me: JwtPrincipal?
    ): ResponseEntity<ApiResponseDto<ThreadDetailRes>> {
        return try {
            val meId = me?.userId
            val detail = threadService.getDetail(threadId, meId)
            ResponseHelper.ok(detail)
        } catch (e: com.rev.app.api.error.ResourceNotFoundException) {
            ResponseHelper.notFound(e.message ?: "게시글을 찾을 수 없습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.notFound("게시글을 찾을 수 없습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseHelper.error("THREAD_DETAIL_FAILED", "게시글을 불러오는 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @Operation(
        summary = "게시글 생성",
        description = "새로운 게시글을 생성합니다.\n\n" +
            "## 요구사항\n" +
            "- 인증 필요 (JWT 토큰)\n" +
            "- 제목: 1-200자\n" +
            "- 내용: 1-10000자\n\n" +
            "## 예제 요청\n" +
            "```json\n" +
            "{\n" +
            "  \"title\": \"게시글 제목\",\n" +
            "  \"content\": \"게시글 내용입니다.\",\n" +
            "  \"isPrivate\": false\n" +
            "}\n" +
            "```"
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "게시글 생성 성공"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 필요"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효성 검사 실패)"
            )
        ]
    )
    @PostMapping("/{boardId}/threads")
    fun createThread(
        @Parameter(description = "게시판 UUID", required = true)
        @PathVariable boardId: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게시글 생성 요청",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ThreadCreateRequest::class),
                examples = [ExampleObject(
                    value = "{\n" +
                        "  \"title\": \"새로운 게시글\",\n" +
                        "  \"content\": \"게시글 내용입니다.\",\n" +
                        "  \"isPrivate\": false\n" +
                        "}"
                )]
            )]
        )
        @RequestBody @Valid req: ThreadCreateRequest,
        @Parameter(hidden = true)
        @AuthenticationPrincipal me: JwtPrincipal?
    ): ResponseEntity<ApiResponseDto<ThreadResponse>> {
        return try {
            val authorId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
            
            val thread = threadService.create(
                boardId = boardId,
                authorId = authorId,
                req = req
            )

            // ThreadResponse.from()은 LAZY 로딩 때문에 null을 반환할 수 있으므로 직접 생성
            val response = ThreadResponse(
                id = thread.id ?: throw IllegalStateException("Thread ID가 생성되지 않았습니다."),
                boardId = boardId,
                authorId = thread.author?.id,
                title = thread.title,
                content = thread.content,
                createdAt = thread.createdAt ?: java.time.Instant.now()
            )
            ResponseHelper.ok(response, "게시글이 생성되었습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.unauthorized(e.message ?: "인증이 필요합니다.")
        } catch (e: Exception) {
            ResponseHelper.error("THREAD_CREATE_FAILED", "게시글 생성 중 오류가 발생했습니다.")
        }
    }

    @Operation(
        summary = "게시글 삭제",
        description = "게시글을 삭제합니다. 관리자 권한이 필요합니다.\n\n" +
            "## 주의사항\n" +
            "- 게시글 삭제 시 관련 댓글도 함께 삭제됩니다.\n" +
            "- 삭제된 게시글은 복구할 수 없습니다."
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "삭제 성공"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (관리자만 가능)"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "게시글을 찾을 수 없음"
            )
        ]
    )
    @DeleteMapping("/{threadId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteThread(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable threadId: UUID
    ): ResponseEntity<ApiResponseDto<Nothing>> {
        return try {
            threadService.delete(threadId)
            ResponseHelper.ok<Nothing>("게시글이 삭제되었습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.notFound("게시글을 찾을 수 없습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("THREAD_DELETE_FAILED", "게시글 삭제 중 오류가 발생했습니다.")
        }
    }
}
