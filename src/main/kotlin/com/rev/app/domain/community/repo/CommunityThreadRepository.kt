// com.rev.app.domain.community.repo.CommunityThreadRepository.kt
package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CommunityThreadRepository : JpaRepository<ThreadEntity, Long>
