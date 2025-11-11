package com.rev.app.api.service.notification

import com.rev.app.domain.notification.NotificationEntity
import com.rev.app.domain.notification.NotificationRepository
import com.rev.test.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

class NotificationServiceTest {

    private val notificationRepository: NotificationRepository =
        Mockito.mock(NotificationRepository::class.java)

    private val service = NotificationService(notificationRepository)

    @Test
    fun markAllRead_marks_everything_and_saves() {
        val uid = UUID.randomUUID()

        // 실행
        service.markAllRead(uid)

        // 최소한 saveAll 호출 검증 (구현과 무관하게 상호작용만 보장)
        Mockito.verify(notificationRepository).saveAll(anyListK())
    }

    @Test
    fun markRead_forbidden_when_not_owner() {
        val myUid = UUID.randomUUID()
        val notifId = UUID.randomUUID()

        val entity = Mockito.mock(NotificationEntity::class.java)
        lenientReturn(Optional.of(entity))
            .`when`(notificationRepository).findById(eqK(notifId))

        assertThrows(IllegalArgumentException::class.java) {
            service.markRead(myUid, notifId)
        }

        Mockito.verify(notificationRepository, Mockito.never())
            .save(anyK(NotificationEntity::class.java))
    }
}
