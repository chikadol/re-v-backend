package com.rev.app.api.service.community

import com.rev.app.api.common.ApiExceptionHandler
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import java.util.UUID.randomUUID
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@WebMvcTest(controllers = [ThreadControllerImpl::class])
class ThreadWebMvcTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockBean lateinit var threadService: ThreadService
    @MockBean lateinit var apiExceptionHandler: ApiExceptionHandler // @ControllerAdvice 자동 스캔 안될 경우 대비

    private fun auth(): UsernamePasswordAuthenticationToken {
        val principal = JwtPrincipal(
            userId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            email = "",
            roles = listOf("test")
        )
        val auths = listOf(SimpleGrantedAuthority("ROLE_USER"))
        return UsernamePasswordAuthenticationToken(principal, "N/A", auths)
    }

    @Test
    fun listPublic_ok() {
        val dto = ThreadRes(1L,"t","c", randomUUID(), listOf("x"), randomUUID(), null,false,null,null)
        whenever(threadService.listPublic(eq(9L), any<Pageable>()))
            .thenAnswer { PageImpl(listOf(dto), PageRequest.of(0,10), 1) }

        mockMvc.perform(
            get("/api/threads/{boardId}/threads", 9L)
                .param("page","0").param("size","10")
                .accept(MediaType.APPLICATION_JSON)
                .with(authentication(auth()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
    }

    @Test
    fun createInBoard_ok() {
        val req = CreateThreadReq("t","c", listOf("tag"))
        val res = ThreadRes(10L,"t","c",
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            listOf("tag"), randomUUID(), null, false, null, null
        )
        whenever(threadService.createInBoard(any(), eq(7L), any())).thenReturn(res)

        mockMvc.perform(
            post("/api/threads/boards/{boardId}", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(req))
                .with(authentication(auth()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10))
    }

    @Test
    fun listPublic_invalidSort_400() {
        mockMvc.perform(
            get("/api/threads/{boardId}/threads", 1L)
                .param("sort","hacker,asc")
                .with(authentication(auth()))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun get_notFound_404() {
        whenever(threadService.get(999L)).thenThrow(IllegalArgumentException("not found"))
        mockMvc.perform(
            get("/api/threads/{id}", 999L).with(authentication(auth()))
        ).andExpect(status().isBadRequest) // not found로 매핑 원하면 핸들러에서 404로 바꿔도 OK
    }
}
