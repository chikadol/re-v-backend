package com.rev.app.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/*@Configuration
class OpenApiConfig {
    @Bean
    fun api(): OpenAPI = OpenAPI().info(
        Info().title("RE:V Server API").version("v1").description("RE:V backend")
    )
}*/

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .components(
            Components().addSecuritySchemes(
                "bearer-key", SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        )
        .addSecurityItem(SecurityRequirement().addList("bearer-key"))
        .info(Info().title("re:v API").version("v1"))
}