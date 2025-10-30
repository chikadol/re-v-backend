// src/main/kotlin/com/rev/app/auth/SecurityConfig.kt
package com.rev.app.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // ✅ 오타 수정 (Mathod → Method)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val p = request.servletPath
        if (
            p == "/" ||
            p.startsWith("/swagger-ui") ||
            p == "/swagger-ui.html" ||
            p.startsWith("/v3/api-docs") ||
            p.startsWith("/swagger-resources") ||
            p.startsWith("/webjars") ||
            p.startsWith("/actuator") ||
            p.startsWith("/api/auth")
        ) {
            filterChain.doFilter(request, response)
            return
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager =
        auth.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                // ✅ 스웨거/문서 경로는 CSRF 예외
                csrf.ignoringRequestMatchers(
                    AntPathRequestMatcher("/v3/api-docs/**"),
                    AntPathRequestMatcher("/swagger-ui/**"),
                    AntPathRequestMatcher("/swagger-ui.html"),
                    AntPathRequestMatcher("/swagger-resources/**"),
                    AntPathRequestMatcher("/webjars/**")
                )
                // 필요 없으면 전체 disable 해도 됨: csrf.disable()
            }
            .cors { } // 필요시 CORS 설정
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // ✅ 스웨거/문서/정적 리소스 허용
                    .requestMatchers(
                        "/",
                        "/error",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()
                    // ✅ 인증 없이 열어둘 API
                    .requestMatchers("/actuator/**", "/api/auth/**").permitAll()
                    // 그 외는 인증 필요
                    .anyRequest().authenticated()
            }
            // ✅ JWT 필터는 UsernamePasswordAuthenticationFilter 앞에
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

        return http.build()
    }
}
