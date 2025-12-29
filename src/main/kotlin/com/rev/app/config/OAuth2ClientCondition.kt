package com.rev.app.config

import org.springframework.boot.autoconfigure.condition.ConditionMessage
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotatedTypeMetadata

class OAuth2ClientCondition : SpringBootCondition() {
    override fun getMatchOutcome(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): ConditionOutcome {
        val environment = context.environment
        
        // 환경 변수에서 직접 읽기 (시스템 프로퍼티와 환경 변수 모두 확인)
        // .env 파일은 Spring Boot가 자동으로 읽지 않으므로, 환경 변수로 직접 설정해야 함
        val googleClientId = System.getenv("GOOGLE_CLIENT_ID")
            ?: environment.getProperty("GOOGLE_CLIENT_ID", "")
            ?: ""
        val naverClientId = System.getenv("NAVER_CLIENT_ID")
            ?: environment.getProperty("NAVER_CLIENT_ID", "")
            ?: ""
        val kakaoClientId = System.getenv("KAKAO_CLIENT_ID")
            ?: environment.getProperty("KAKAO_CLIENT_ID", "")
            ?: ""
        
        // 디버깅 로그
        println("🔍 OAuth2ClientCondition 체크:")
        println("  GOOGLE_CLIENT_ID: ${if (googleClientId.isNotBlank()) "설정됨 (길이: ${googleClientId.length})" else "없음"}")
        println("  NAVER_CLIENT_ID: ${if (naverClientId.isNotBlank()) "설정됨 (길이: ${naverClientId.length})" else "없음"}")
        println("  KAKAO_CLIENT_ID: ${if (kakaoClientId.isNotBlank()) "설정됨 (길이: ${kakaoClientId.length})" else "없음"}")
        
        val hasAnyClient = !googleClientId.isNullOrBlank() || 
                          !naverClientId.isNullOrBlank() || 
                          !kakaoClientId.isNullOrBlank()
        
        return if (hasAnyClient) {
            println("✅ OAuth2ClientCondition: Bean 생성 허용")
            ConditionOutcome.match(
                ConditionMessage.forCondition("OAuth2ClientCondition")
                    .found("OAuth2 client configuration").items("Google, Naver, or Kakao")
            )
        } else {
            println("❌ OAuth2ClientCondition: Bean 생성 거부 (환경 변수 없음)")
            ConditionOutcome.noMatch(
                ConditionMessage.forCondition("OAuth2ClientCondition")
                    .didNotFind("OAuth2 client configuration").atAll()
            )
        }
    }
}

