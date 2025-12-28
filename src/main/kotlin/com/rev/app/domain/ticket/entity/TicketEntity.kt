package com.rev.app.domain.ticket.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ticket", schema = "rev")
class TicketEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    var performance: PerformanceEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Column(nullable = false)
    var price: Int, // 구매 가격

    @Column
    var seatNumber: String? = null, // 좌석 번호 (선택사항)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TicketStatus = TicketStatus.PENDING, // 대기, 확정, 취소

    var purchaseDate: Instant? = Instant.now(),

    var createdAt: Instant? = Instant.now()
)

enum class TicketStatus {
    PENDING,   // 결제 대기
    CONFIRMED, // 확정
    CANCELLED  // 취소
}

