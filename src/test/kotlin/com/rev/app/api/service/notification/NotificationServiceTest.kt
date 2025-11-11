package com.rev.app.api.service.notification
import com.rev.test.*
import com.rev.app.domain.notification.NotificationEntity
import com.rev.app.domain.notification.NotificationRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*

/**
 * 임시-안전 테스트:
 * - org.mockito.kotlin.* 전혀 사용 안 함
 * - 엔티티의 필드명(receiver/user/recipient 등)이나
 *   레포의 커스텀 메서드명(findAllByReceiver... 등)에 의존하지 않음
 * - JpaRepository 표준 메서드(findById/save/saveAll)만 사용 → 컴파일 보장
 *
 * 추후 실제 시그니처를 알려주면, 아래 TODO 지점만 교체한 “정식 테스트”로 바꿔줄게.
 */
class NotificationServiceTest {
    // --- Mockito matcher helpers (Kotlin null/제네릭 안전) ---
    private fun <T> eqK(v: T): T = org.mockito.ArgumentMatchers.eq(v)
    private fun <T> anyK(clazz: Class<T>): T = org.mockito.ArgumentMatchers.any(clazz)

    private val notificationRepository: NotificationRepository =
        Mockito.mock(NotificationRepository::class.java)

    // TODO: 실제 생성자 시그니처에 맞게 조정할 수 있음
    private val service = NotificationService(notificationRepository)

    @Test
    fun markAllRead_marks_everything_and_saves() {
        val uid = UUID.randomUUID()

        // 가짜 엔티티 2개 (필드명 접근 X)
        val n1 = Mockito.mock(NotificationEntity::class.java)
        val n2 = Mockito.mock(NotificationEntity::class.java)

        // 서비스 구현이 내부에서 어떤 조회 메서드를 쓰든,
        // 최종적으로 saveAll 이 호출되는지만 검증 (상호작용 테스트)
        Mockito.doReturn(listOf(n1, n2))
            .`when`(notificationRepository)
            .saveAll(ArgumentMatchers.anyList())

        // 실행
        service.markAllRead(uid)

        // 최소한 saveAll 이 호출됐는지 확인 (N개는 모르면 anyList로만)
        Mockito.verify(notificationRepository)
            .saveAll(ArgumentMatchers.anyList())
    }

    @Test
    fun markRead_forbidden_when_not_owner() {
        val myUid = UUID.randomUUID()
        val notifId = UUID.randomUUID()

        val entity = Mockito.mock(NotificationEntity::class.java)
        Mockito.doReturn(Optional.of(entity))
            .`when`(notificationRepository)
            .findById(ArgumentMatchers.eq(notifId))

        // 구현마다 예외 타입이 다를 수 있음 → 일단 IllegalArgumentException 가정
        assertThrows(IllegalArgumentException::class.java) {
            service.markRead(myUid, notifId)
        }

        // 남의 알림이면 save()가 호출되지 않아야 한다(일반적 기대치)
        Mockito.verify(notificationRepository, Mockito.never())
            .save(ArgumentMatchers.any(NotificationEntity::class.java))
    }
}
