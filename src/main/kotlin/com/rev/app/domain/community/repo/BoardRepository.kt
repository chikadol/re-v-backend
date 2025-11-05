package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Board
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BoardRepository : JpaRepository<Board, UUID>
