package com.rev.app.api.interceptor

import io.github.bucket4j.Bucket
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Rate Limiting 인터셉터
 * API 요청 빈도를 제한하여 서버 보호
 */
@Component
class RateLimitInterceptor(
    @Qualifier("apiBucket") private val apiBucket: Bucket,
    @Qualifier("authBucket") private val authBucket: Bucket,
    @Qualifier("searchBucket") private val searchBucket: Bucket
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val path = request.requestURI

        // 경로별로 다른 버킷 사용
        val bucket = when {
            path.startsWith("/api/auth/") -> authBucket
            path.contains("/search") || path.contains("/api/youtube/") -> searchBucket
            else -> apiBucket
        }

        // 토큰 소비 시도
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            // 남은 토큰 수를 헤더에 추가
            response.setHeader("X-RateLimit-Remaining", probe.remainingTokens.toString())
            return true
        } else {
            // Rate limit 초과
            val waitTime = probe.nanosToWaitForRefill / 1_000_000_000 // 나노초를 초로 변환
            response.setHeader("X-RateLimit-Retry-After", waitTime.toString())
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(
                """
                {
                    "success": false,
                    "error": {
                        "code": "RATE_LIMIT_EXCEEDED",
                        "message": "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
                    }
                }
                """.trimIndent()
            )
            return false
        }
    }
}

