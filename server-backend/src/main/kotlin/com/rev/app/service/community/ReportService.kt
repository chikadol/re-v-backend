package com.rev.app.service.community

import com.rev.app.domain.community.*
import com.rev.app.domain.community.repo.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class ReportReq(val reason: String?, val detail: String?)

@Service
class ReportService(
    private val threadReports: ThreadReportRepository,
    private val commentReports: CommentReportRepository
) {
    @Transactional fun reportThread(threadId: Long, reporterId: Long, req: ReportReq): ThreadReport =
        threadReports.save(ThreadReport(threadId = threadId, reporterId = reporterId, reason = req.reason, detail = req.detail, createdAt = Instant.now()))

    @Transactional fun reportComment(commentId: Long, reporterId: Long, req: ReportReq): CommentReport =
        commentReports.save(CommentReport(commentId = commentId, reporterId = reporterId, reason = req.reason, detail = req.detail, createdAt = Instant.now()))
}
