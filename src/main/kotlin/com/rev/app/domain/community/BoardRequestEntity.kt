package com.rev.app.domain.community

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "board_request", schema = "rev")
class BoardRequestEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var slug: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(columnDefinition = "text")
    var reason: String? = null, // 생성 요청 사유

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    var requester: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BoardRequestStatus = BoardRequestStatus.PENDING, // PENDING, APPROVED, REJECTED

    @Column(name = "created_at")
    var createdAt: Instant? = Instant.now(),

    @Column(name = "processed_at")
    var processedAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    var processedBy: UserEntity? = null // 처리한 관리자
)

enum class BoardRequestStatus {
    PENDING,    // 대기 중
    APPROVED,   // 승인됨
    REJECTED    // 거부됨
}

