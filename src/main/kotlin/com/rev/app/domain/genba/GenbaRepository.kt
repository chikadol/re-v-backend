package com.rev.app.domain.genba

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface GenbaRepository : JpaRepository<Genba, Long> {
    @Query("""
select g from Genba g
where (:dateFrom is null or g.startAt >= :dateFrom)
  and (:dateTo   is null or g.startAt <  :dateTo)
  and (:area     is null or g.areaCode = :area)
  and (:q        is null or lower(g.title) like lower(concat('%', :q, '%')))
  and (:cursorAt is null or (g.startAt < :cursorAt or (g.startAt = :cursorAt and g.id < :cursorId)))
order by case when :sort = 'POPULAR' then g.popularityScore end desc, g.startAt desc, g.id desc
""")
    fun findGenbas(
        dateFrom: Instant?,
        dateTo: Instant?,
        area: String?,
        q: String?,
        cursorAt: Instant?,
        cursorId: Long?,
        sort: String,
        pageable: Pageable
    ): List<Genba>
}
