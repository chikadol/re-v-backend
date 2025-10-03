package com.rev.app.support

import com.github.javafaker.Faker
import com.rev.app.domain.artist.Artist
import com.rev.app.domain.artist.ArtistRepository
import com.rev.app.domain.genba.Genba
import com.rev.app.domain.genba.GenbaRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

@Configuration
class SeedConfig {
    @Bean
    fun seedData(artists: ArtistRepository, genbas: GenbaRepository) = CommandLineRunner {
        if (artists.count() > 0 || genbas.count() > 0) return@CommandLineRunner
        val faker = Faker()

        val artistEntities = (1..40).map {
            Artist(
                stageName = faker.rockBand().name(),
                stageNameKr = null,
                groupName = listOf(null, "Alpha", "Beta", "Gamma").random(),
                tags = arrayOf("idol", "rookie"),
                debutDate = LocalDate.now().minusDays(Random.nextLong(1000)),
                avatarUrl = null,
                popularityScore = Random.nextInt(0, 1000)
            )
        }
        artists.saveAll(artistEntities)

        val now = Instant.now()
        val genbaEntities = (1..60).map {
            val start = now.minusSeconds(Random.nextLong(60L*60*24*60)).plusSeconds(Random.nextLong(60L*60*24*60))
            Genba(
                title = "Genba #$it",
                description = "Sample event $it",
                startAt = start,
                endAt = start.plusSeconds(7200),
                areaCode = listOf("KR-11","KR-26","KR-28","KR-27").random(),
                placeName = "Hall ${Random.nextInt(1,100)}",
                address = "Somewhere",
                posterUrl = null,
                popularityScore = Random.nextInt(0, 1000)
            )
        }
        genbas.saveAll(genbaEntities)
    }
}
