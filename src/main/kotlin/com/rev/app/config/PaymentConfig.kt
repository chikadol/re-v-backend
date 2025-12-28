package com.rev.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

/**
 * 결제 서비스 설정
 */
@Configuration
class PaymentConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        val factory: ClientHttpRequestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(10000) // 10초
            setReadTimeout(30000) // 30초
        }
        return RestTemplate(factory)
    }
}

