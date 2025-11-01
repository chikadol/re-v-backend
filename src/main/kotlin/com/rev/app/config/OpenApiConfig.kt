// src/main/kotlin/com/rev/app/config/OpenApiConfig.kt
package com.rev.app.config

import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val scheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .`in`(SecurityScheme.In.HEADER)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("Authorization")
        return OpenAPI()
            .info(Info().title("re-v API").version("v1"))
            .components(Components().addSecuritySchemes("bearerAuth", scheme))
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
}
