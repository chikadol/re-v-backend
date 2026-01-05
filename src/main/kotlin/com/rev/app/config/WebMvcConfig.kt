package com.rev.app.config

import com.rev.app.api.interceptor.RateLimitInterceptor
import com.rev.app.api.security.MeArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val meArgumentResolver: MeArgumentResolver,
    private val rateLimitInterceptor: RateLimitInterceptor
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(meArgumentResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/health", // 헬스 체크는 제외
                "/swagger-ui/**",
                "/v3/api-docs/**"
            )
    }
}
