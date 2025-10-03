package com.rev.app.domain.community.repo

import com.rev.app.domain.community.ThreadReport
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadReportRepository : JpaRepository<ThreadReport, Long>
