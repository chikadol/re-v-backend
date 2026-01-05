package com.rev.app.config

import com.rev.app.auth.JwtAuthenticationFilter
import com.rev.app.auth.OAuth2SuccessHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    
    @Autowired(required = false)
    private var oAuth2SuccessHandler: OAuth2SuccessHandler? = null

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
    fun filterChain(
        http: HttpSecurity,
        clientRegistrationRepository: ClientRegistrationRepository?
    ): SecurityFilterChain {

        // ✅ OAuth2 로그인 설정 (클라이언트가 설정된 경우에만)
        if (clientRegistrationRepository != null && oAuth2SuccessHandler != null) {
            try {
                println("✅ OAuth2 로그인 활성화: clientRegistrationRepository 존재")
                // OAuth2 로그인은 세션을 사용하므로 세션 관리 활성화
                // 세션 관리를 먼저 설정해야 OAuth2 로그인이 제대로 작동함
                http
                    .sessionManagement { session ->
                        // OAuth2 로그인 플로우를 위해 세션 필요
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    }
                    .oauth2Login { oauth2 ->
                        oauth2
                            .successHandler(oAuth2SuccessHandler)
                            .failureHandler { request, response, exception ->
                                println("❌ OAuth2 로그인 실패: ${exception.message}")
                                exception.printStackTrace()
                                // 프론트엔드 로그인 페이지로 리다이렉트
                                response.sendRedirect("http://localhost:5173/login?error=oauth2_failed&message=${java.net.URLEncoder.encode(exception.message ?: "OAuth2 로그인 실패", "UTF-8")}")
                            }
                            .userInfoEndpoint { userInfo ->
                                userInfo.userService(org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService())
                            }
                    }
            } catch (e: Exception) {
                // OAuth2 설정이 없으면 무시
                println("⚠️ OAuth2 설정 오류: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("⚠️ OAuth2 로그인 비활성화: clientRegistrationRepository=${clientRegistrationRepository != null}, oAuth2SuccessHandler=${oAuth2SuccessHandler != null}")
            // OAuth2가 없으면 세션 사용 안 함 (JWT 사용)
            http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        }

        http
            // ✅ 세션/폼로그인/베이직 인증 다 끄기 (브라우저 팝업 사라짐)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

            // ✅ CSRF 끄기 (Swagger / curl에서 POST 403 안 나게)
            // 단, OAuth2 로그인은 CSRF를 사용하지 않으므로 괜찮음
            .csrf { it.disable() }

            // ✅ CORS 설정
            .cors { it.configurationSource(corsConfigurationSource()) }

        http
            // ✅ JWT 필터 추가 (OAuth2 경로는 JWT 필터에서 자동으로 건너뜀)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            // ✅ 모든 요청은 일단 허용 (로컬 개발 단계용)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll() // 모니터링 엔드포인트 허용
                    .anyRequest().permitAll()
            }

        return http.build()
    }
}
