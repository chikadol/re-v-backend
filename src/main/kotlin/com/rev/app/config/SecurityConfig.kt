package com.rev.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    // 허용하는 공개 엔드포인트가 있다면 여기에 permitAll()
                    // 예: it.requestMatchers("/health").permitAll()
                    .requestMatchers("/api/threads/**").authenticated()
                    .anyRequest().permitAll()
            }
            .httpBasic { } // 401을 쉽게 재현하려면 basic/on도 OK (실서비스는 JWT)
        return http.build()
    }
}
