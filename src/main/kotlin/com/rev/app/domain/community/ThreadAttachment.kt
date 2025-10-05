package com.rev.app.domain.community

import jakarta.persistence.*

@Entity
@Table(name = "thread_attachment", schema = "rev")
class ThreadAttachment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var threadId: Long,
    @Column(nullable = false)
    var type: String,
    @Column(nullable = false)
    var path: String,
    var width: Int? = null,
    var height: Int? = null,
    var duration: Int? = null,
    var orderNo: Int = 0,
    @Column(columnDefinition = "jsonb")
    var metadata: String? = "{}"
)
