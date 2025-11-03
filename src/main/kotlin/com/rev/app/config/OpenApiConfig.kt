package com.rev.app.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration(proxyBeanMethods = false)
@Profile("!test") // ✅ 테스트 프로필에서는 아예 제외(권장)
class OpenApiConfig {
    @Bean
    fun openAPI(): io.swagger.v3.oas.models.OpenAPI =
        io.swagger.v3.oas.models.OpenAPI().info(
            io.swagger.v3.oas.models.info.Info()
                .title("re:v API")
                .version("v1")
        )
}
