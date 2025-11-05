package com.rev.app.domain.community.repo

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BoardRepository : JpaRepository<com.rev.app.domain.community.Board, UUID>
