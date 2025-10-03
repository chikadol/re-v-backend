package com.jihamdol.jihamdolapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    @Value("\${payment.toss.base-url}") private val tossBaseUrl: String
) {
    @Bean
    fun tossWebClient(): WebClient = WebClient.builder()
        .baseUrl(tossBaseUrl)
        .build()
}
