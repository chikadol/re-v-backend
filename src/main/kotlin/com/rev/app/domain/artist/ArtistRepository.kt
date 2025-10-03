package com.rev.app.domain.artist

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ArtistRepository : JpaRepository<Artist, Long> {
    @Query("""
select a from Artist a
where (:q is null or lower(a.stageName) like lower(concat('%', :q, '%')) or lower(a.stageNameKr) like lower(concat('%', :q, '%')) or lower(a.groupName) like lower(concat('%', :q, '%')))
order by case when :sort = 'POPULAR' then a.popularityScore end desc, a.stageName asc
""")
    fun search(q: String?, sort: String, pageable: Pageable): List<Artist>
}
