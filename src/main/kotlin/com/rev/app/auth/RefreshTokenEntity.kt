import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "refresh_token", schema = "rev")
class RefreshTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ bigserial과 매칭
    @Column(name = "id")
    var id: Long? = null,                                // ✅ Long

    @Column(name = "username", nullable = false, length = 100)
    var username: String,

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "text")
    var token: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "revoked", nullable = false)
    var revoked: Boolean = false,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
