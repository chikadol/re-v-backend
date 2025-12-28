package com.rev.app.domain.ticket.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "performance", schema = "rev")
class PerformanceEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(nullable = false)
    var venue: String, // 공연장

    @Column(nullable = false)
    var performanceDateTime: LocalDateTime, // 공연 일시

    @Column(nullable = false)
    var price: Int, // 티켓 가격

    @Column(nullable = false)
    var totalSeats: Int, // 총 좌석 수

    @Column(nullable = false)
    var remainingSeats: Int, // 남은 좌석 수

    @Column
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PerformanceStatus = PerformanceStatus.UPCOMING, // 예정, 진행중, 종료

    var createdAt: Instant? = Instant.now(),

    var updatedAt: Instant? = Instant.now()
)

enum class PerformanceStatus {
    UPCOMING,    // 예정
    ONGOING,     // 진행중
    ENDED        // 종료
}

