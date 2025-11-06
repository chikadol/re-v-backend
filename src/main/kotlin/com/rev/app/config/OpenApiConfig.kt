package com.rev.app.config


import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OpenApiConfig {
    @Bean
    fun apiInfo(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Community API")
                .version("v1")
                .description("Threads, Comments, Bookmarks")
        )
}

