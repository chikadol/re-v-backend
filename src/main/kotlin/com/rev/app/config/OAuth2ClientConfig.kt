package com.rev.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

@Configuration
class OAuth2ClientConfig(
    private val environment: Environment
) {

    @Bean
    @Conditional(OAuth2ClientCondition::class)
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        println("🔧 OAuth2ClientConfig.clientRegistrationRepository() 호출됨")
        
        // Environment에서 환경 변수 읽기 (시스템 환경 변수와 Spring 프로퍼티 모두 확인)
        val googleClientId = System.getenv("GOOGLE_CLIENT_ID")
            ?: environment.getProperty("GOOGLE_CLIENT_ID", "")
        val googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET")
            ?: environment.getProperty("GOOGLE_CLIENT_SECRET", "")
        val naverClientId = System.getenv("NAVER_CLIENT_ID")
            ?: environment.getProperty("NAVER_CLIENT_ID", "")
        val naverClientSecret = System.getenv("NAVER_CLIENT_SECRET")
            ?: environment.getProperty("NAVER_CLIENT_SECRET", "")
        val kakaoClientId = System.getenv("KAKAO_CLIENT_ID")
            ?: environment.getProperty("KAKAO_CLIENT_ID", "")
        val kakaoClientSecret = System.getenv("KAKAO_CLIENT_SECRET")
            ?: environment.getProperty("KAKAO_CLIENT_SECRET", "")
        
        println("📋 환경 변수 확인:")
        println("  GOOGLE_CLIENT_ID: ${if (googleClientId.isNotBlank()) "설정됨 (길이: ${googleClientId.length})" else "없음"}")
        println("  GOOGLE_CLIENT_SECRET: ${if (googleClientSecret.isNotBlank()) "설정됨 (길이: ${googleClientSecret.length})" else "없음"}")
        println("  NAVER_CLIENT_ID: ${if (naverClientId.isNotBlank()) "설정됨 (길이: ${naverClientId.length})" else "없음"}")
        println("  NAVER_CLIENT_SECRET: ${if (naverClientSecret.isNotBlank()) "설정됨 (길이: ${naverClientSecret.length})" else "없음"}")
        
        val registrations = mutableListOf<ClientRegistration>()

        // Google OAuth2
        if (!googleClientId.isNullOrBlank() && !googleClientSecret.isNullOrBlank()) {
            registrations.add(
                ClientRegistration.withRegistrationId("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .build()
            )
        }

        // Naver OAuth2
        if (!naverClientId.isNullOrBlank() && !naverClientSecret.isNullOrBlank()) {
            registrations.add(
                ClientRegistration.withRegistrationId("naver")
                    .clientId(naverClientId)
                    .clientSecret(naverClientSecret)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("name", "email")
                    .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                    .tokenUri("https://nid.naver.com/oauth2.0/token")
                    .userInfoUri("https://openapi.naver.com/v1/nid/me")
                    .userNameAttributeName("response")
                    .build()
            )
        }

        // Kakao OAuth2
        if (!kakaoClientId.isNullOrBlank() && !kakaoClientSecret.isNullOrBlank()) {
            registrations.add(
                ClientRegistration.withRegistrationId("kakao")
                    .clientId(kakaoClientId)
                    .clientSecret(kakaoClientSecret)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("profile_nickname", "account_email")
                    .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                    .tokenUri("https://kauth.kakao.com/oauth/token")
                    .userInfoUri("https://kapi.kakao.com/v2/user/me")
                    .userNameAttributeName("id")
                    .build()
            )
        }

        // @Conditional(OAuth2ClientCondition::class)로 인해 이 메서드는 
        // OAuth2 클라이언트가 하나라도 설정되어 있을 때만 호출됨
        // 따라서 registrations는 항상 비어있지 않음
        require(registrations.isNotEmpty()) {
            "OAuth2 클라이언트가 설정되어 있지 않습니다. 이 Bean은 생성되지 않아야 합니다."
        }
        
        println("✅ ClientRegistrationRepository 생성 완료: ${registrations.size}개 등록 (${registrations.map { it.registrationId }.joinToString(", ")})")
        return InMemoryClientRegistrationRepository(registrations)
    }
}

