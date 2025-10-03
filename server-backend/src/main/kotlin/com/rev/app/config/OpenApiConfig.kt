package com.rev.app.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun api(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("RE:V Community API")
                .version("v1")
                .description("로그인/겐바/아티스트 + 커뮤니티 API")
                .contact(Contact().name("RE:V").url("https://example.com"))
        )
}
