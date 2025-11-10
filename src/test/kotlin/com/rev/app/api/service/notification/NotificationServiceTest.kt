package com.rev.app.api.service.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import com.rev.app.api.service.notification.dto.NotificationRes
import com.rev.app.domain.notification.NotificationEntity
import com.rev.app.domain.notification.NotificationRepository
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.Optional
import java.util.UUID

class NotificationServiceTest {

    private val repo: NotificationRepository = mock()
    private val service = NotificationService(repo)

    private fun sampleEntities(ownerId: UUID): Triple<NotificationEntity, ThreadEntity, CommentEntity> {
        val user = UserEntity(id = ownerId, email = "u@x.com", username = "u", password = "p")
        val thread = ThreadEntity(title = "t", content = "c").apply { id = UUID.randomUUID() }
        val comment = CommentEntity(
            thread = thread,
            author = user,
            content = "hello"
        ).apply { id = UUID.randomUUID() }

        val n = NotificationEntity(
            user = user,
            type = "COMMENT",
            thread = thread,
            comment = comment,
            message = "새 댓글: hello",
            isRead = false,
            createdAt = Instant.now()
        ).apply { id = UUID.randomUUID() }

        return Triple(n, thread, comment)
    }

    @Test
    fun listMine_ok() {
        val uid = UUID.randomUUID()
        val (n, _, _) = sampleEntities(uid)

        whenever(repo.findAllByUser_IdOrderByCreatedAtDesc(eq(uid), any<Pageable>()))
            .thenReturn(PageImpl(listOf(n)))

        val page = service.listMine(uid, Pageable.ofSize(10))
        assertEquals(1, page.totalElements)
        val item: NotificationRes = page.content.first()
        assertEquals(n.id, item.id)
        assertEquals(n.thread.id, item.threadId)
        assertEquals(n.comment.id, item.commentId)
        assertFalse(item.isRead)
    }

    @Test
    fun markRead_ok() {
        val uid = UUID.randomUUID()
        val (n, _, _) = sampleEntities(uid)

        whenever(repo.findById(n.id!!)).thenReturn(Optional.of(n))
        whenever(repo.save(any())).thenAnswer { it.arguments[0] }

        val res = service.markRead(uid, n.id!!)
        assertTrue(res.isRead)
        verify(repo).save(check {
            assertTrue(it.isRead)
            assertEquals(n.id, it.id)
        })
    }

    @Test
    fun markRead_forbidden() {
        val owner = UUID.randomUUID()
        val other = UUID.randomUUID()
        val (n, _, _) = sampleEntities(owner)

        whenever(repo.findById(n.id!!)).thenReturn(Optional.of(n))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.markRead(other, n.id!!)
        }
        assertEquals("Forbidden", ex.message)
        verify(repo, never()).save(any())
    }

    @Test
    fun markAllRead_marks_everything_and_saves() {
        val uid = UUID.randomUUID()
        val (n1, _, _) = sampleEntities(uid)
        val (n2, _, _) = sampleEntities(uid)

        whenever(repo.findAllByUser_IdOrderByCreatedAtDesc(eq(uid), any<Pageable>()))
            .thenReturn(PageImpl(listOf(n1, n2)))

        service.markAllRead(uid)

        verify(repo).saveAll(check<List<NotificationEntity>> {
            assertTrue(it.all { e -> e.isRead }, "all notifications should be marked read")
            assertEquals(setOf(n1.id, n2.id), it.map { e -> e.id }.toSet())
        })
    }

    @Test
    fun unreadCount_ok() {
        val uid = UUID.randomUUID()
        whenever(repo.countByUser_IdAndIsReadFalse(uid)).thenReturn(3L)

        val count = service.unreadCount(uid)
        assertEquals(3L, count)
    }
}
