package com.rev.app.api.service.idol

import com.rev.app.api.service.idol.dto.IdolRes
import com.rev.app.domain.idol.IdolEntity
import com.rev.app.domain.idol.IdolRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IdolService(
    private val idolRepository: IdolRepository
) {
    @Transactional(readOnly = true)
    fun list(): List<IdolRes> =
        idolRepository.findAll().map { IdolRes.from(it) }

    @Transactional(readOnly = true)
    fun get(id: UUID): IdolRes =
        IdolRes.from(idolRepository.findById(id).orElseThrow { IllegalArgumentException("아이돌을 찾을 수 없습니다.") })

    @Transactional
    fun create(name: String, description: String?, imageUrl: String?): IdolRes {
        if (idolRepository.existsByName(name)) {
            throw IllegalArgumentException("이미 존재하는 아이돌 이름입니다.")
        }
        val saved = idolRepository.save(
            IdolEntity(
                name = name,
                description = description,
                imageUrl = imageUrl
            )
        )
        return IdolRes.from(saved)
    }

    @Transactional
    fun deleteAll() {
        idolRepository.deleteAll()
    }

    @Transactional
    fun delete(id: UUID) {
        if (!idolRepository.existsById(id)) {
            throw IllegalArgumentException("아이돌을 찾을 수 없습니다.")
        }
        idolRepository.deleteById(id)
    }
}

