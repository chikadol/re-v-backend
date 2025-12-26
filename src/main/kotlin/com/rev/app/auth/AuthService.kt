package com.rev.app.auth

import com.rev.app.auth.jwt.JwtProvider
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {
    fun loginByEmail(email: String, rawPassword: String): TokenPair {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid credentials")

        // TODO: 비밀번호 검증(PasswordEncoder) 추가

        val uid: UUID = userIdOf(user)
            ?: throw IllegalStateException("User id is null")
        val username: String = usernameOf(user)

        val roles: List<String> = rolesOf(user)

        val access  = jwtProvider.generateAccessToken(uid, username, roles)
        val refresh = jwtProvider.generateRefreshToken(
            uid,
            email = TODO(),
            listOf = TODO()
        )
        return TokenPair(access, refresh)
    }

    fun refresh(refreshToken: String): TokenPair {
        if (!jwtProvider.validate(refreshToken) || !jwtProvider.isRefresh(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }
        val uid: UUID = jwtProvider.getUserId(refreshToken)
        val user = userRepository.findById(uid).orElseThrow()
        val username: String = usernameOf(user)
        val roles: List<String> = rolesOf(user)

        val newAccess  = jwtProvider.generateAccessToken(uid, username, roles)
        val newRefresh = jwtProvider.generateRefreshToken(
            uid,
            email = TODO(),
            listOf = TODO()
        )
        return TokenPair(newAccess, newRefresh)
    }

    // ---------------------- helpers (reflection only) ----------------------

    private fun userIdOf(user: Any): UUID? {
        // id: UUID?
        return try {
            (user::class.java.getMethod("getId").invoke(user) as? UUID)
        } catch (_: Exception) { null }
    }

    private fun usernameOf(user: Any): String {
        // 우선순위: getUsername() -> getEmail() -> toString()
        return tryGet<String>(user, "getUsername")
            ?: tryGet<String>(user, "getEmail")
            ?: user.toString()
    }

    private fun rolesOf(user: Any): List<String> {
        // 1) getRoles(): Collection<*> (각 요소에 getName() 있으면 name, 아니면 toString)
        try {
            val m = user::class.java.getMethod("getRoles")
            val v = m.invoke(user)
            if (v is Collection<*>) {
                return v.mapNotNull { elem ->
                    // elem.getName() -> String
                    tryGet<String>(elem ?: return@mapNotNull null, "getName")
                        ?: elem.toString()
                }
            }
        } catch (_: Exception) { /* ignore */ }

        // 2) getRole(): String
        try {
            val m = user::class.java.getMethod("getRole")
            (m.invoke(user) as? String)?.let { return listOf(it) }
        } catch (_: Exception) { /* ignore */ }

        // 3) getAuthorities(): Collection<GrantedAuthority> -> each.getAuthority(): String
        try {
            val m = user::class.java.getMethod("getAuthorities")
            val v = m.invoke(user)
            if (v is Collection<*>) {
                return v.mapNotNull { ga ->
                    tryGet<String>(ga ?: return@mapNotNull null, "getAuthority")
                }
            }
        } catch (_: Exception) { /* ignore */ }

        // 없으면 빈 리스트
        return emptyList()
    }

    private inline fun <reified T> tryGet(target: Any, methodName: String): T? =
        try { target::class.java.getMethod(methodName).invoke(target) as? T }
        catch (_: Exception) { null }
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
