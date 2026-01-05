package com.rev.app.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Rate Limiting 설정
 * Bucket4j를 사용한 API 요청 제한
 */
@Configuration
class RateLimitConfig {

    /**
     * 일반 API 요청 제한: 분당 100회
     */
    @Bean("apiBucket")
    fun apiBucket(): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    100,
                    Refill.intervally(100, Duration.ofMinutes(1))
                )
            )
            .build()
    }

    /**
     * 인증 API 요청 제한: 분당 10회 (무차별 대입 공격 방지)
     */
    @Bean("authBucket")
    fun authBucket(): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    10,
                    Refill.intervally(10, Duration.ofMinutes(1))
                )
            )
            .build()
    }

    /**
     * 검색 API 요청 제한: 분당 50회
     */
    @Bean("searchBucket")
    fun searchBucket(): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    50,
                    Refill.intervally(50, Duration.ofMinutes(1))
                )
            )
            .build()
    }
}

