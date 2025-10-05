package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "thread_report", schema = "rev")
class ThreadReport(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var threadId: Long,
    @Column(nullable = false)
    var reporterId: Long,
    @Enumerated(EnumType.STRING)
    var status: ReportStatus = ReportStatus.OPEN,
    var reason: String? = null,
    @Column(columnDefinition = "text")
    var detail: String? = null,
    var createdAt: Instant = Instant.now(),
    var resolvedBy: Long? = null,
    var resolvedAt: Instant? = null
)
