package com.rev.app.api.controller
import com.rev.test.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rev.app.api.service.community.ReactionService
import com.rev.app.api.service.community.dto.ToggleReactionRes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID
import com.rev.test.*

@ExtendWith(MockitoExtension::class)
class ReactionControllerWebMvcTest {

    private val FIXED_UID = UUID.fromString("11111111-1111-1111-1111-111111111111")

    private class PermissivePrincipalResolver(
        private val uid: UUID
    ) : HandlerMethodArgumentResolver {
        private val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        override fun supportsParameter(p: org.springframework.core.MethodParameter) =
            p.hasParameterAnnotation(AuthenticationPrincipal::class.java) ||
                    (p.parameterType.simpleName.lowercase().contains("jwt") && p.parameterType.simpleName.lowercase().contains("principal"))
        override fun resolveArgument(
            p: org.springframework.core.MethodParameter,
            mav: ModelAndViewContainer?, req: NativeWebRequest, bf: WebDataBinderFactory?
        ): Any = mapper.convertValue(
            mapOf("userId" to uid, "email" to "mock@test.com", "roles" to listOf("USER")),
            p.parameterType
        )
    }

    private fun mvc(controller: Any): MockMvc {
        val om = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        return MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver(), PermissivePrincipalResolver(FIXED_UID))
            .setMessageConverters(MappingJackson2HttpMessageConverter(om))
            .build()
    }

        @Mock
        lateinit var service: ReactionService
        private lateinit var mockMvc: MockMvc

        @BeforeEach
        fun setup() {
            mockMvc = MockMvcBuilders.standaloneSetup(ReactionController(service)).build()
        }

        @Test
        fun toggle_ok() {
            val threadId = UUID.randomUUID()
            lenientReturn(ToggleReactionRes(true, mapOf("LIKE" to 1L, "LOVE" to 0L)))
                .`when`(service).toggle(
                    eqK(FIXED_UID),
                    eqK(threadId),
                    eqK("LIKE")
                )


            mockMvc.perform(post("/api/threads/$threadId/reactions/LIKE"))
                .andExpect(status().isOk)

    mockMvc.perform(
            post("/api/threads/$threadId/reactions/LIKE").accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }
}
