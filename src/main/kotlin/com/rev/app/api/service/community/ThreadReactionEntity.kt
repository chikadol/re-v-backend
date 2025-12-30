package com.rev.app.domain.community.model

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "thread_reaction", schema = "rev",
    uniqueConstraints = [UniqueConstraint(columnNames = ["thread_id","user_id","type"])]
)
class ThreadReactionEntity(
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity?,

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity?,

    @Column(name = "type", nullable = false, length = 20)
    var type: String, // "LIKE", "LOVE"...

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
) {
    // H2가 자동 생성한 reaction 컬럼과의 호환성을 위한 속성
    // type과 항상 동일한 값을 저장
    @Column(name = "reaction", nullable = false, length = 20)
    var reaction: String = ""
        get() = type
    
    @PrePersist
    @PreUpdate
    fun syncReactionBeforeSave() {
        reaction = type
    }
}
