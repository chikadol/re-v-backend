package com.rev.app.domain.community.repo

import com.rev.app.domain.community.CommentReport
import org.springframework.data.jpa.repository.JpaRepository

interface CommentReportRepository : JpaRepository<CommentReport, Long>
