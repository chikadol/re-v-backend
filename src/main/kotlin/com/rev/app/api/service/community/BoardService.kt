package com.rev.app.api.service.community
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.toRes

import com.rev.app.domain.community.repo.BoardRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.collections.map

@Service
class BoardService(
    private val boardRepository: BoardRepository
) {
    fun list(): List<BoardRes> =
        boardRepository.findAll().map { it.toRes() }

    fun get(id: UUID): BoardRes =
        boardRepository.findById(id).orElseThrow().toRes()
}
