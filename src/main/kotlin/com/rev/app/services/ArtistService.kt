package com.rev.app.services

import com.rev.app.api.ArtistItem
import com.rev.app.domain.artist.ArtistRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ArtistService(private val repo: ArtistRepository) {
    fun list(q: String?, sort: String, size: Int, page: Int): List<ArtistItem> =
        repo.search(q, sort, PageRequest.of(page, size)).map {
            ArtistItem(
                id = it.id!!,
                stageName = it.stageName,
                stageNameKr = it.stageNameKr,
                groupName = it.groupName,
                avatarUrl = it.avatarUrl,
                popularityScore = it.popularityScore
            )
        }
}
