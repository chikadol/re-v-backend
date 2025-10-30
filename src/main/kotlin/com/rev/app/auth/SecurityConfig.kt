// src/main/kotlin/com/rev/app/auth/SecurityConfig.kt
package com.rev.app.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration                     // 클래스에 붙임
@EnableWebSecurity                 // 클래스에 붙임
@EnableMathodSecurity(prePostEnabled = true) // 클래스에 붙임 (오타나면 컴파일 에러)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter // 이 필터는 @Component 로 빈 등록되어 있어야 함
) {

    @Bean                          // 메서드에는 @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager =
        auth.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { } // 필요 시 CORS 설정 추가
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/error", "/actuator/**", "/api/auth/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

annotation class EnableMathodSecurity(val prePostEnabled: Boolean)
