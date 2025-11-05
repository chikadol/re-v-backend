package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.domain.community.repo.BoardRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BoardService(
    private val boardRepository: BoardRepository
) {
    @Transactional(readOnly = true)
    fun get(id: UUID): BoardRes =
        boardRepository.findById(id).orElseThrow().toRes()

    @Transactional(readOnly = true)
    fun list(): List<BoardRes> =
        boardRepository.findAll().map { it.toRes() }

    @Transactional
    fun create(name: String, slug: String, description: String?): BoardRes =
        boardRepository.save(com.rev.app.domain.community.Board(name = name, slug = slug, description = description))
            .toRes()
}
