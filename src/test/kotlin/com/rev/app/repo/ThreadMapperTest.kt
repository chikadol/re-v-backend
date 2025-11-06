package com.rev.app.repo

import com.rev.app.api.service.community.dto.toRes
import com.rev.app.domain.community.entity.ThreadEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ThreadMapperTest {
    @Test
    fun roundTrip_ok() {
        val entity = ThreadEntity(
            title = "t",
            content = "c",
            isPrivate = false
        )
        val dto = entity.toRes()
        assertThat(dto.title).isEqualTo("t")
        assertThat(dto.content).isEqualTo("c")
    }
}
