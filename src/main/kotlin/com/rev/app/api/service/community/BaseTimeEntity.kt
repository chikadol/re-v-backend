package com.rev.app.api.service.community

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@MappedSuperclass
abstract class BaseTimeEntity {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: java.time.OffsetDateTime

    @UpdateTimestamp
    @Column(name = "updated_at")
    lateinit var updatedAt: java.time.OffsetDateTime
}