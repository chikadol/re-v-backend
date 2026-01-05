package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import org.springframework.core.MethodParameter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

class FakePrincipalResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(p: MethodParameter): Boolean =
        p.hasParameterAnnotation(AuthenticationPrincipal::class.java) &&
                p.parameterType == JwtPrincipal::class.java

    override fun resolveArgument(
        p: MethodParameter, mav: ModelAndViewContainer?, req: NativeWebRequest,
        binder: WebDataBinderFactory?
    ): Any = JwtPrincipal(
        userId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        email = "test@example.com",
        roles = listOf("USER")
    )
}
