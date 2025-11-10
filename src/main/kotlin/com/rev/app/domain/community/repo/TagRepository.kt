package com.rev.app.domain.community.repo

import com.rev.app.domain.community.model.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TagRepository : JpaRepository<TagEntity, UUID> {
    fun findByNameIn(names: Collection<String>): List<TagEntity>
    fun findByName(name: String): TagEntity?
}