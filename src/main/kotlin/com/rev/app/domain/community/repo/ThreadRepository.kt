package com.rev.app.domain.community.repo

import ThreadEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<ThreadEntity, UUID>
