import com.rev.app.api.controller.ReactionController
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ReactionService
import com.rev.app.api.service.community.dto.ToggleReactionRes
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any as anyK
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post as mvcPost // ✅ alias
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

class ReactionControllerWebMvcTest {
    private val service: ReactionService = mock()

    private class FakeAuthPrincipalResolver(
        private val fixedUserId: UUID
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            val hasAnno = parameter.hasParameterAnnotation(AuthenticationPrincipal::class.java)
            val isType = JwtPrincipal::class.java.isAssignableFrom(parameter.parameterType)
            return hasAnno && isType
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any = JwtPrincipal(
            userId = fixedUserId,
            email = "t@example.com",
            roles = listOf("TEST")
        )
    }

    @Test
    fun toggle_ok() {
        val controller = ReactionController(service)
        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                FakeAuthPrincipalResolver(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            )
            .build()

        val threadId = UUID.randomUUID()

        whenever(service.toggle(anyK(), anyK(), anyK()))
            .thenReturn(ToggleReactionRes(toggled = true, counts = mapOf("LIKE" to 1L, "LOVE" to 0L)))

        mockMvc.perform(
            mvcPost("/api/threads/$threadId/reactions/LIKE") // ✅ MockMvc용 post 사용
                .accept(MediaType.APPLICATION_JSON)          // ✅ accept 해석 가능
        ).andExpect(status().isOk)
    }
}