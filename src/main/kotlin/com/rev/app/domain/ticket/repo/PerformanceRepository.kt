package com.rev.app.domain.ticket.repo

import com.rev.app.domain.ticket.entity.PerformanceEntity
import com.rev.app.domain.ticket.entity.PerformanceStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PerformanceRepository : JpaRepository<PerformanceEntity, UUID> {
    fun findAllByStatusOrderByPerformanceDateTimeAsc(status: PerformanceStatus): List<PerformanceEntity>
    fun findAllByOrderByPerformanceDateTimeAsc(): List<PerformanceEntity>
    fun findAllByIdol_IdOrderByPerformanceDateTimeAsc(idolId: UUID): List<PerformanceEntity>
}

