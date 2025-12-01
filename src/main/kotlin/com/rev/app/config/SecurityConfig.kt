package com.rev.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            // ✅ 세션/폼로그인/베이직 인증 다 끄기 (브라우저 팝업 사라짐)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

            // ✅ CSRF 끄기 (Swagger / curl에서 POST 403 안 나게)
            .csrf { it.disable() }

            // ✅ 모든 요청은 일단 허용 (로컬 개발 단계용)
            .authorizeHttpRequests { auth ->
                auth
                    .anyRequest().permitAll()
            }

        return http.build()
    }
}
