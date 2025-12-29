package com.rev.app.config

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * OAuth2ClientProperties를 빈 값으로도 생성할 수 있도록 하는 설정
 * Spring Boot 자동 설정이 빈 클라이언트 ID를 검증하지 않도록 함
 * 
 * OAuth2ClientProperties는 afterPropertiesSet()에서 검증을 수행하므로,
 * 검증을 우회하기 위해 커스텀 클래스를 만들어야 합니다.
 */
@Configuration
@EnableConfigurationProperties
class OAuth2ClientPropertiesConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.security.oauth2.client")
    fun oAuth2ClientProperties(): OAuth2ClientProperties {
        // 검증을 우회하기 위해 커스텀 클래스 사용
        return NonValidatingOAuth2ClientProperties()
    }
    
    /**
     * 검증을 수행하지 않는 OAuth2ClientProperties
     */
    private class NonValidatingOAuth2ClientProperties : OAuth2ClientProperties() {
        override fun afterPropertiesSet() {
            // 검증을 수행하지 않음 (빈 클라이언트 ID 허용)
            // 실제 사용은 OAuth2ClientConfig에서 환경 변수로 직접 읽음
        }
    }
}

