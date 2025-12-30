package com.rev.app.domain.idol

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IdolRepository : JpaRepository<IdolEntity, UUID> {
    fun existsByName(name: String): Boolean
    fun findByNameIgnoreCase(name: String): IdolEntity?
}

