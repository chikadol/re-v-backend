package com.rev.app.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun api(): OpenAPI = OpenAPI().info(
        Info().title("RE:V Server API").version("v1").description("RE:V backend")
    )
}
