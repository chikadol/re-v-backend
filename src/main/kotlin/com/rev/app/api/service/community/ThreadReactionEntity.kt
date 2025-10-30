package com.rev.app.api.service.community

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread_reaction", schema = "rev")
class ThreadReactionEntity(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:Column(name = "thread_id", nullable = false)
    val threadId: Long,

    @field:Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "type", nullable = false)
    val type: ReactionType,

    ) : BaseTimeEntity() // 시간 컬럼은 상속으로만