package com.rev.app.api.service.community

import com.rev.app.api.controller.PageResponse
import com.rev.app.api.controller.dto.ThreadCreateRequest
import com.rev.app.api.controller.dto.ThreadResponse
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadDetailRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.api.service.community.dto.toResWithTags
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.model.TagEntity
import com.rev.app.domain.community.model.ThreadTagEntity
import com.rev.app.domain.community.repo.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val threadTagRepository: ThreadTagRepository,
    private val commentRepository: CommentRepository,
    private val bookmarkRepository: ThreadBookmarkRepository,
    private val reactionRepository: ThreadReactionRepository,
) {
    private val allowedReactions = setOf("LIKE", "LOVE")

    @Cacheable(value = ["threadDetail"], key = "#threadId.toString() + '_' + (#meId?.toString() ?: 'anonymous')")
    fun getDetail(
        threadId: UUID,
        meId: UUID? = null
    ): ThreadDetailRes {
        val thread = getThreadWithRelations(threadId)
        
        val commentCount = getCommentCount(threadId)
        val bookmarkCount = getBookmarkCount(threadId)
        val reactions = getReactions(threadId)
        val myReaction = getMyReaction(threadId, meId)
        val bookmarked = getBookmarked(threadId, meId)

        return ThreadDetailRes(
            thread = thread.toRes(),
            commentCount = commentCount,
            bookmarkCount = bookmarkCount,
            reactions = reactions,
            myReaction = myReaction,
            bookmarked = bookmarked
        )
    }
    
    @Transactional(readOnly = true)
    private fun getThreadWithRelations(threadId: UUID): ThreadEntity {
        // JOIN FETCH로 N+1 문제 해결
        val thread = threadRepository.findByIdWithRelations(threadId)
            ?: throw IllegalArgumentException("Thread not found: $threadId")
        
        return thread
    }
    
    private fun getCommentCount(threadId: UUID): Long {
        return try {
            commentRepository.countByThread_Id(threadId)
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getBookmarkCount(threadId: UUID): Long {
        return try {
            bookmarkRepository.countByThread_Id(threadId)
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getReactions(threadId: UUID): Map<String, Long> {
        return try {
            allowedReactions.associateWith { type -> 
                reactionRepository.countByThread_IdAndType(threadId, type)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    private fun getMyReaction(threadId: UUID, meId: UUID?): String? {
        return try {
            if (meId != null) {
                allowedReactions.firstOrNull { type ->
                    reactionRepository.findByThread_IdAndUser_IdAndType(threadId, meId, type) != null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getBookmarked(threadId: UUID, meId: UUID?): Boolean {
        return try {
            if (meId != null) {
                bookmarkRepository.findByThread_IdAndUser_Id(threadId, meId) != null
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private val tagRegex = Regex("^[A-Za-z0-9_-]{1,30}$")
    private val maxTags = 5

    @CacheEvict(value = ["threads"], allEntries = true) // 게시글 생성 시 목록 캐시 무효화
    @Transactional
    fun createInBoard(
        userId: UUID,
        boardId: UUID,
        req: CreateThreadReq
    ): ThreadRes {
        // ✅ 먼저 태그 검증 (여기서 예외가 터지면 DB 접근 안 함)
        val names = normalizeAndValidate(req.tags)

        val board = boardRepository.getReferenceById(boardId)
        val author = userRepository.getReferenceById(userId)

        val entity = ThreadEntity(
            title = req.title,
            content = req.content,
            board = board,
            author = author,
            isPrivate = false,
            categoryId = null,
            parent = null
        )
        val saved = threadRepository.save(entity)

        if (names.isNotEmpty()) attachTags(saved.id!!, names)

        val tags = threadTagRepository.findByThread_Id(saved.id!!).map { it.tag.name }
        return saved.toResWithTags(tags)
    }
    // ✅ 기존 테스트 호환: 태그 없이 목록
    @Transactional(readOnly = true)
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> {
        val page = threadRepository.findByBoard_IdAndIsPrivateFalse(boardId, pageable)
        
        // N+1 문제 해결: 배치로 태그 조회
        val threadIds = page.content.mapNotNull { it.id }
        val tagsMap = if (threadIds.isNotEmpty()) {
            threadTagRepository.findByThread_IdIn(threadIds)
                .groupBy { it.thread?.id }
                .mapValues { (_, tags) -> tags.map { it.tag.name } }
        } else {
            emptyMap()
        }
        
        return page.map { thread ->
            val tagNames = tagsMap[thread.id] ?: emptyList()
            thread.toResWithTags(tagNames)
        }
    }

    /**
     * PageResponse를 반환하는 캐시 가능한 메서드
     * PageImpl 대신 PageResponse를 캐싱하여 역직렬화 문제 해결
     */
    @Cacheable(
        value = ["threads"],
        key = "#boardId.toString() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + (#tags?.joinToString(',') ?: 'none')"
    )
    @Transactional(readOnly = true)
    fun listPublicAsPageResponse(
        boardId: UUID,
        pageable: Pageable,
        tags: List<String>? = null
    ): PageResponse<ThreadRes> {
        val page = if (tags.isNullOrEmpty()) {
            listPublic(boardId, pageable)
        } else {
            listPublic(boardId, pageable, tags)
        }
        
        return PageResponse(
            content = page.content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            number = page.number,
            size = page.size,
            first = page.isFirst,
            last = page.isLast
        )
    }

    /**
     * 검색 결과를 PageResponse로 반환하는 캐시 가능한 메서드
     */
    @Cacheable(
        value = ["threads"],
        key = "#boardId.toString() + '_search_' + #keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    fun searchAsPageResponse(
        boardId: UUID,
        keyword: String,
        pageable: Pageable
    ): PageResponse<ThreadRes> {
        val page = search(boardId, keyword, pageable)
        
        return PageResponse(
            content = page.content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            number = page.number,
            size = page.size,
            first = page.isFirst,
            last = page.isLast
        )
    }

    // ✅ 새 기능: 태그 필터 버전
    @Transactional(readOnly = true)
    fun listPublic(boardId: UUID, pageable: Pageable, tags: List<String>?): Page<ThreadRes> {
        val names = (tags ?: emptyList())
            .map { it.trim().lowercase() }
            .filter { it.matches(tagRegex) }

        if (names.isEmpty()) {
            // 필터가 비면 기존 경로 사용 → 레거시 테스트와 동일 동작
            return listPublic(boardId, pageable)
        }

        val page = threadRepository.findPublicByBoardWithAnyTags(
            boardId = boardId,
            names = names,
            namesEmpty = false,
            pageable = pageable
        )

        // N+1 문제 해결: 배치로 태그 조회
        val threadIds = page.content.mapNotNull { it.id }
        val tagsMap = if (threadIds.isNotEmpty()) {
            threadTagRepository.findByThread_IdIn(threadIds)
                .groupBy { it.thread?.id }
                .mapValues { (_, tags) -> tags.map { it.tag.name } }
        } else {
            emptyMap()
        }

        return page.map { thread ->
            val tagNames = tagsMap[thread.id] ?: emptyList()
            thread.toResWithTags(tagNames)
        }
    }

    // 검색 기능: 제목과 내용으로 검색
    @Transactional(readOnly = true)
    fun search(boardId: UUID, keyword: String, pageable: Pageable): Page<ThreadRes> {
        if (keyword.isBlank()) {
            return listPublic(boardId, pageable)
        }
        
        val page = threadRepository.findByBoard_IdAndIsPrivateFalseAndTitleOrContentContaining(
            boardId = boardId,
            keyword = keyword.trim(),
            pageable = pageable
        )
        
        // N+1 문제 해결: 배치로 태그 조회
        val threadIds = page.content.mapNotNull { it.id }
        val tagsMap = if (threadIds.isNotEmpty()) {
            threadTagRepository.findByThread_IdIn(threadIds)
                .groupBy { it.thread?.id }
                .mapValues { (_, tags) -> tags.map { it.tag.name } }
        } else {
            emptyMap()
        }
        
        return page.map { thread ->
            val tagNames = tagsMap[thread.id] ?: emptyList()
            thread.toResWithTags(tagNames)
        }
    }

    private fun normalizeAndValidate(raw: List<String>?): List<String> {
        val list = (raw ?: emptyList())
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        require(list.size <= maxTags) { "Too many tags (max=$maxTags)" }
        list.forEach { name -> require(name.matches(tagRegex)) { "Invalid tag: $name" } }
        return list.distinct()
    }

    private fun attachTags(threadId: UUID, names: List<String>) {
        val existing = tagRepository.findByNameIn(names).associateBy { it.name }
        val toCreate = names.filterNot(existing::containsKey).map { TagEntity(name = it) }
        val created = if (toCreate.isNotEmpty()) tagRepository.saveAll(toCreate) else emptyList()
        val all = names.map { existing[it] ?: created.first { c -> c.name == it } }

        val threadRef = threadRepository.getReferenceById(threadId)
        all.forEach { tag ->
            threadTagRepository.save(ThreadTagEntity(thread = threadRef, tag = tag))
        }
    }

    @Transactional(readOnly = true)
    fun listMyThreads(
        authorId: UUID,
        pageable: Pageable
    ): Page<ThreadRes> =
        threadRepository
            .findAllByAuthor_Id(authorId, pageable)
            .map { it.toRes() }

    fun create(
        boardId: UUID,
        authorId: UUID,
        req: ThreadCreateRequest
    ): ThreadEntity {   // ✅ 반환 타입을 ThreadEntity로 명시

        val board = boardRepository.getReferenceById(boardId)
        val author = userRepository.getReferenceById(authorId)

        val entity = ThreadEntity(
            board = board,
            author = author,
            title = req.title,
            content = req.content,
            isPrivate = req.isPrivate ?: false
        )

        return threadRepository.saveAndFlush(entity)   // ✅ flush하여 ID와 createdAt이 즉시 반영되도록
    }

    @CacheEvict(value = ["threadDetail", "threads"], allEntries = true) // 게시글 삭제 시 관련 캐시 무효화
    @Transactional
    fun delete(threadId: UUID) {
        if (!threadRepository.existsById(threadId)) {
            throw IllegalArgumentException("게시글을 찾을 수 없습니다: $threadId")
        }
        threadRepository.deleteById(threadId)
    }

}
