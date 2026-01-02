package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRequestCreateRequest
import com.rev.app.api.service.community.dto.BoardRequestProcessRequest
import com.rev.app.api.service.community.dto.BoardRequestRes
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.BoardRequestEntity
import com.rev.app.domain.community.BoardRequestStatus
import com.rev.app.domain.community.repo.BoardRequestRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class BoardRequestService(
    private val boardRequestRepository: BoardRequestRepository,
    private val userRepository: UserRepository,
    private val boardService: BoardService
) {
    @Transactional
    fun create(requesterId: UUID, request: BoardRequestCreateRequest): BoardRequestRes {
        val requester = userRepository.getReferenceById(requesterId)
        
        // slug 중복 확인 (대기 중인 요청 포함)
        val existingRequest = boardRequestRepository.findAll().find { 
            it.slug == request.slug && it.status == BoardRequestStatus.PENDING 
        }
        if (existingRequest != null) {
            throw IllegalArgumentException("이미 대기 중인 요청이 있습니다: ${request.slug}")
        }
        
        // 이미 존재하는 게시판 slug 확인
        val existingBoards = boardService.list()
        if (existingBoards.any { it.slug == request.slug }) {
            throw IllegalArgumentException("이미 사용 중인 slug입니다: ${request.slug}")
        }
        
        val entity = BoardRequestEntity(
            name = request.name,
            slug = request.slug,
            description = request.description,
            reason = request.reason,
            requester = requester,
            status = BoardRequestStatus.PENDING
        )
        
        val saved = boardRequestRepository.saveAndFlush(entity)
        return saved.toRes()
    }
    
    @Transactional(readOnly = true)
    fun listPending(pageable: Pageable): Page<BoardRequestRes> {
        return boardRequestRepository.findAllByStatusOrderByCreatedAtDesc(
            BoardRequestStatus.PENDING,
            pageable
        ).map { it.toRes() }
    }
    
    @Transactional(readOnly = true)
    fun listMyRequests(requesterId: UUID, pageable: Pageable): Page<BoardRequestRes> {
        return boardRequestRepository.findAllByRequester_IdOrderByCreatedAtDesc(
            requesterId,
            pageable
        ).map { it.toRes() }
    }
    
    @Transactional
    fun process(adminId: UUID, requestId: UUID, processRequest: BoardRequestProcessRequest): BoardRequestRes {
        val admin = userRepository.getReferenceById(adminId)
        val boardRequest = boardRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("게시판 생성 요청을 찾을 수 없습니다.") }
        
        if (boardRequest.status != BoardRequestStatus.PENDING) {
            throw IllegalArgumentException("이미 처리된 요청입니다.")
        }
        
        boardRequest.processedBy = admin
        boardRequest.processedAt = Instant.now()
        
        if (processRequest.approved) {
            boardRequest.status = BoardRequestStatus.APPROVED
            // 게시판 생성
            boardService.create(
                com.rev.app.api.service.community.dto.BoardCreateRequest(
                    name = boardRequest.name,
                    slug = boardRequest.slug,
                    description = boardRequest.description
                )
            )
        } else {
            boardRequest.status = BoardRequestStatus.REJECTED
        }
        
        val saved = boardRequestRepository.saveAndFlush(boardRequest)
        return saved.toRes()
    }
    
    @Transactional(readOnly = true)
    fun getPendingCount(): Long {
        return boardRequestRepository.countByStatus(BoardRequestStatus.PENDING)
    }
}

