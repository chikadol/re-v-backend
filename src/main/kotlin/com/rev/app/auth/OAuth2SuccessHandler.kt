package com.rev.app.auth

import com.rev.app.auth.dto.TokenResponse
import com.rev.app.auth.jwt.JwtProvider
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Component
class OAuth2SuccessHandler(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder,
    private val objectMapper: ObjectMapper
) : SimpleUrlAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        logger.info("=".repeat(80))
        logger.info("OAuth2 로그인 성공 핸들러 실행 시작")
        logger.info("요청 URI: ${request.requestURI}")
        logger.info("요청 메서드: ${request.method}")
        logger.info("인증 타입: ${authentication.javaClass.simpleName}")
        logger.info("=".repeat(80))
        
        try {
            // OAuth2AuthenticationToken에서 registrationId 추출
            val oauth2Token = authentication as? OAuth2AuthenticationToken
                ?: throw IllegalStateException("OAuth2AuthenticationToken을 찾을 수 없습니다.")
            
            val registrationId = oauth2Token.authorizedClientRegistrationId
            logger.info("OAuth2 registrationId: $registrationId")
            
            val oauth2User = authentication.principal as OAuth2User
            val attributes = oauth2User.attributes
            
            logger.info("OAuth2 사용자 속성: $attributes")

            // Provider별 사용자 정보 추출
            val (email, username, providerId) = when (registrationId) {
                "google" -> extractGoogleUserInfo(attributes)
                "naver" -> extractNaverUserInfo(attributes)
                "kakao" -> extractKakaoUserInfo(attributes)
                else -> throw IllegalArgumentException("지원하지 않는 OAuth2 Provider: $registrationId")
            }

            // 사용자 조회 또는 생성
            val user = findOrCreateUser(email, username, registrationId, providerId)

            // JWT 토큰 생성
            val tokenResponse = user.id?.let { userId ->
                TokenResponse(
                    accessToken = jwtProvider.generateAccessToken(userId),
                    refreshToken = jwtProvider.generateRefreshToken(userId)
                )
            } ?: throw IllegalStateException("사용자 ID가 없습니다.")

            // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
            // 절대 URL을 사용하여 프론트엔드로 직접 리다이렉트
            val redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth/callback")
                .queryParam("accessToken", tokenResponse.accessToken)
                .queryParam("refreshToken", tokenResponse.refreshToken)
                .queryParam("provider", registrationId)
                .build()
                .toUriString()

            logger.info("OAuth2 로그인 완료, 프론트엔드로 리다이렉트: $redirectUrl")
            
            // 응답이 이미 커밋되었는지 확인
            if (response.isCommitted) {
                logger.warn("응답이 이미 커밋되었습니다. 리다이렉트를 수행할 수 없습니다.")
                return
            }
            
            // 절대 URL로 프론트엔드로 리다이렉트
            // response.sendRedirect()를 사용하여 명확하게 리다이렉트 처리
            response.sendRedirect(redirectUrl)
            
            // 리다이렉트 후 더 이상 처리하지 않음
            return
            
        } catch (e: Exception) {
            logger.error("=".repeat(80))
            logger.error("OAuth2 로그인 처리 중 오류 발생")
            logger.error("에러 타입: ${e.javaClass.simpleName}")
            logger.error("에러 메시지: ${e.message}")
            logger.error("스택 트레이스:", e)
            logger.error("=".repeat(80))
            e.printStackTrace()
            
            // 에러 발생 시 프론트엔드 로그인 페이지로 리다이렉트
            try {
                val errorRedirectUrl = "http://localhost:5173/login?error=oauth2_failed&message=${java.net.URLEncoder.encode(e.message ?: "알 수 없는 오류", "UTF-8")}"
                logger.error("에러 페이지로 리다이렉트: $errorRedirectUrl")
                response.status = HttpServletResponse.SC_MOVED_TEMPORARILY
                response.setHeader("Location", errorRedirectUrl)
                response.flushBuffer()
                return
            } catch (redirectException: Exception) {
                logger.error("리다이렉트 실패", redirectException)
                // 리다이렉트도 실패하면 에러 응답
                response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                response.contentType = "application/json;charset=UTF-8"
                response.writer.write("""{"error":"OAuth2 로그인 처리 실패","message":"${e.message}"}""")
                response.flushBuffer()
            }
        }
    }

    private fun extractGoogleUserInfo(attributes: Map<String, Any>): Triple<String, String, String> {
        val email = attributes["email"] as? String
            ?: throw IllegalStateException("Google에서 이메일을 가져올 수 없습니다.")
        val name = attributes["name"] as? String ?: email.split("@").first()
        val providerId = attributes["sub"] as? String
            ?: throw IllegalStateException("Google에서 사용자 ID를 가져올 수 없습니다.")
        return Triple(email, name, providerId)
    }

    private fun extractNaverUserInfo(attributes: Map<String, Any>): Triple<String, String, String> {
        val response = attributes["response"] as? Map<*, *>
            ?: throw IllegalStateException("Naver 응답 형식이 올바르지 않습니다.")
        val email = response["email"] as? String
            ?: throw IllegalStateException("Naver에서 이메일을 가져올 수 없습니다.")
        val name = response["name"] as? String ?: response["nickname"] as? String
            ?: email.split("@").first()
        val providerId = response["id"] as? String
            ?: throw IllegalStateException("Naver에서 사용자 ID를 가져올 수 없습니다.")
        return Triple(email, name, providerId)
    }

    private fun extractKakaoUserInfo(attributes: Map<String, Any>): Triple<String, String, String> {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
            ?: throw IllegalStateException("Kakao 응답 형식이 올바르지 않습니다.")
        val email = kakaoAccount["email"] as? String
            ?: throw IllegalStateException("Kakao에서 이메일을 가져올 수 없습니다.")
        val profile = kakaoAccount["profile"] as? Map<*, *>
        val name = profile?.get("nickname") as? String
            ?: email.split("@").first()
        val providerId = attributes["id"]?.toString()
            ?: throw IllegalStateException("Kakao에서 사용자 ID를 가져올 수 없습니다.")
        return Triple(email, name, providerId)
    }

    private fun findOrCreateUser(
        email: String,
        username: String,
        provider: String,
        providerId: String
    ): UserEntity {
        // Provider ID로 먼저 조회
        val existingByProvider = userRepository.findByProviderAndProviderId(provider, providerId)
        if (existingByProvider != null) {
            logger.info("기존 OAuth2 사용자 로그인: $email (provider=$provider)")
            return existingByProvider
        }

        // 이메일로 조회 (기존 일반 회원가입 사용자)
        val existingByEmail = userRepository.findByEmail(email)
        if (existingByEmail != null) {
            // 기존 사용자에 OAuth2 정보 추가
            existingByEmail.provider = provider
            existingByEmail.providerId = providerId
            logger.info("기존 사용자에 OAuth2 연동: $email (provider=$provider)")
            return userRepository.save(existingByEmail)
        }

        // 새 사용자 생성
        val newUser = UserEntity(
            email = email,
            username = username,
            password = passwordEncoder.encode(UUID.randomUUID().toString()), // OAuth2 사용자는 랜덤 비밀번호
            provider = provider,
            providerId = providerId
        )
        logger.info("새 OAuth2 사용자 생성: $email (provider=$provider)")
        return userRepository.saveAndFlush(newUser)
    }
}

