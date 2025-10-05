package com.rev.app.domain.community.repo

import com.rev.app.domain.community.BoardManager
import com.rev.app.domain.community.BoardManagerId
import org.springframework.data.jpa.repository.JpaRepository

interface BoardManagerRepository : JpaRepository<BoardManager, BoardManagerId>
