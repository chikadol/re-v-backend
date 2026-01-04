package com.rev.app.auth

import com.rev.app.api.controller.ResponseHelper
import com.rev.app.api.controller.dto.ApiResponse
import com.rev.app.api.security.JwtPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class MeProfileDto(
    val userId: String, // UUID를 문자열로 반환
    val username: String,
    val roles: List<String>
)

@RestController
class MeController(
    private val userRepository: UserRepository
) {
    @GetMapping("/api/me")
    fun me(@AuthenticationPrincipal principal: JwtPrincipal?): ResponseEntity<ApiResponse<MeProfileDto>> {
        val me = principal ?: throw IllegalArgumentException("인증이 필요합니다.")
        val userId = me.userId ?: throw IllegalArgumentException("사용자 ID가 없습니다.")
        
        // 사용자 정보 조회
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        val profile = MeProfileDto(
            userId = userId.toString(),
            username = user.username,
            roles = me.roles
        )
        return ResponseHelper.ok(profile)
    }
}
