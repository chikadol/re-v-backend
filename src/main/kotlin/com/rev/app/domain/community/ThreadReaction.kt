package com.rev.app.domain.community

import com.rev.app.common.jpa.BaseTime
import com.rev.app.domain.community.entity.ThreadEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "thread_reaction", schema = "rev")
class ThreadReaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity
) : BaseTime()
