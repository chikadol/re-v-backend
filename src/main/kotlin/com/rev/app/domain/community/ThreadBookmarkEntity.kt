import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "thread_bookmark",
    schema = "rev",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "thread_id"])]
)
data class ThreadBookmarkEntity(

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid")
    var id: UUID? = null,   // ← PK 반드시 필요! var 권장

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    // 스키마에 맞춰 타입 선택: UUID이면 UUID, BIGINT이면 Long
    @Column(name = "thread_id", nullable = false /*, columnDefinition = "uuid"*/)
    val threadId: UUID,     // ← 스레드가 BIGINT면 Long 로 바꿔

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
