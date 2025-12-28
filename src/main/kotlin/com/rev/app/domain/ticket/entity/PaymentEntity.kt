package com.rev.app.domain.ticket.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "payment", schema = "rev")
class PaymentEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: TicketEntity? = null,

    @Column(nullable = false)
    var amount: Int, // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentMethod: PaymentMethod, // 네이버페이, 토스, 카카오페이

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING, // 대기, 완료, 실패, 취소

    @Column
    var paymentId: String? = null, // 외부 결제 시스템 ID

    var paidAt: Instant? = null,

    var createdAt: Instant? = Instant.now()
)

enum class PaymentMethod {
    NAVER_PAY,  // 네이버페이
    TOSS,       // 토스
    KAKAO_PAY   // 카카오페이
}

enum class PaymentStatus {
    PENDING,  // 대기
    COMPLETED, // 완료
    FAILED,    // 실패
    CANCELLED  // 취소
}

