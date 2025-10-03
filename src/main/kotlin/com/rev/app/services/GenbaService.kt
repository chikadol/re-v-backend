package com.rev.app.services

import com.rev.app.api.GenbaItem
import com.rev.app.domain.genba.GenbaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GenbaService(private val repo: GenbaRepository) {
    fun list(
        dateFrom: Instant?, dateTo: Instant?, area: String?, q: String?,
        cursorAt: Instant?, cursorId: Long?, sort: String, size: Int, page: Int
    ): List<GenbaItem> =
        repo.findGenbas(dateFrom, dateTo, area, q, cursorAt, cursorId, sort, PageRequest.of(page, size)).map {
            GenbaItem(
                id = it.id!!,
                title = it.title,
                startAt = it.startAt,
                endAt = it.endAt,
                areaCode = it.areaCode,
                placeName = it.placeName,
                posterUrl = it.posterUrl,
                popularityScore = it.popularityScore
            )
        }
}
