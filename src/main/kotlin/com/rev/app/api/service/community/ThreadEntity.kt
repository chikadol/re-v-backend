import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(schema = "rev", name = "thread")
class ThreadEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(nullable = false, length = 200)
    var title: String,

    @Lob
    @Column(nullable = false)
    var content: String,

    @Column(name = "author_id", nullable = false)
    var authorId: UUID,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        schema = "rev",
        name = "thread_tag",
        joinColumns = [JoinColumn(name = "thread_id")]
    )
    @Column(name = "tag", length = 50)
    var tags: MutableList<String> = mutableListOf(),

    @Column(name = "category_id")
    var categoryId: UUID? = null,

    @Column(name = "parent_thread_id")
    var parentThreadId: UUID? = null,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)