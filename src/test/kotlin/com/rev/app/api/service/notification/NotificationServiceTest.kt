package com.rev.app.api.service.notification

import com.rev.app.domain.notification.NotificationEntity
import com.rev.app.domain.notification.NotificationRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*
import org.junit.jupiter.api.Disabled

@Disabled("임시 비활성화 - 테스트 환경/Mockito 정리 후 다시 살릴 예정")
class NotificationServiceTest {

    private val notificationRepository: NotificationRepository =
        Mockito.mock(NotificationRepository::class.java)

    private val service = NotificationService(notificationRepository)

    @Test
    fun markAllRead_marks_everything_and_saves() {
        val uid = UUID.randomUUID()
        service.markAllRead(uid)
        Mockito.verify(notificationRepository).saveAll(ArgumentMatchers.anyList())
    }

    @Test
    fun markRead_forbidden_when_not_owner() {
        val myUid = UUID.randomUUID()
        val notifId = UUID.randomUUID()

        val entity = Mockito.mock(NotificationEntity::class.java)

        Mockito.lenient().doReturn(Optional.of(entity))
            .`when`(notificationRepository).findById(ArgumentMatchers.eq(notifId))

        assertThrows(IllegalArgumentException::class.java) {
            service.markRead(myUid, notifId)
        }

        Mockito.verify(notificationRepository, Mockito.never())
            .save(ArgumentMatchers.any(NotificationEntity::class.java))
    }
}
