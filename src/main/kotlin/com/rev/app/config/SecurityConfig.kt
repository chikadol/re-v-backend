package com.rev.app.config

import com.rev.app.auth.JwtAuthenticationFilter
import com.rev.app.auth.OAuth2SuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("http://localhost:*", "http://127.0.0.1:*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("Authorization")
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            // ✅ 세션/폼로그인/베이직 인증 다 끄기 (브라우저 팝업 사라짐)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

            // ✅ CSRF 끄기 (Swagger / curl에서 POST 403 안 나게)
            .csrf { it.disable() }

            // ✅ 세션 사용 안 함 (JWT 사용)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // ✅ CORS 설정
            .cors { it.configurationSource(corsConfigurationSource()) }

            // ✅ OAuth2 로그인 설정
            .oauth2Login { oauth2 ->
                oauth2
                    .successHandler(oAuth2SuccessHandler)
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService())
                    }
            }

            // ✅ JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            // ✅ 모든 요청은 일단 허용 (로컬 개발 단계용)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .anyRequest().permitAll()
            }

        return http.build()
    }
}
