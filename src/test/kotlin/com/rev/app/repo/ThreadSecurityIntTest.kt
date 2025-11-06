package com.rev.app.repo

import com.rev.app.RevApplication
import com.rev.app.api.security.MeArgumentResolver
import com.rev.app.api.service.community.ThreadController
import com.rev.app.api.service.community.ThreadService
import com.rev.app.auth.JwtAuthenticationFilter
import com.rev.app.auth.jwt.JwtProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(controllers = [ThreadController::class])
@AutoConfigureMockMvc // (기본값) 보안 필터 활성화
class ThreadSecurityIntTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    // 컨트롤러가 의존하는 서비스/리졸버/보안 컴포넌트는 전부 MockBean으로 대체
    @MockBean lateinit var threadService: ThreadService
    @MockBean lateinit var meArgumentResolver: MeArgumentResolver
    @MockBean lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @Test
    fun rejectWithoutAuth_401() {
        // UUID 경로 변수로 전달 (Long 사용 시 NPE/IAE 유발 가능)
        val boardId = UUID.randomUUID()

        mockMvc.get("/api/threads/{boardId}/threads", boardId) {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isUnauthorized() } // 401 기대
        }
    }
}
