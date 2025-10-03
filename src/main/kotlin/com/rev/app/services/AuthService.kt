package com.rev.app.services

import com.rev.app.core.user.User
import com.rev.app.core.user.UserRepository
import com.rev.app.core.user.UserSession
import com.rev.app.core.user.UserSessionRepository
import com.rev.app.security.JwtService
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.time.Instant

data class Tokens(val accessToken: String, val refreshToken: String)

@Service
class AuthService(
    private val users: UserRepository,
    private val sessions: UserSessionRepository,
    private val jwt: JwtService
) {
    fun signup(email: String, password: String) {
        require(users.findByEmail(email) == null) { "EMAIL_EXISTS" }
        val u = User(email = email, passwordHash = BCrypt.hashpw(password, BCrypt.gensalt()))
        users.save(u)
    }

    fun login(email: String, password: String): Tokens {
        val u = users.findByEmail(email) ?: throw IllegalArgumentException("NO_USER")
        if (!BCrypt.checkpw(password, u.passwordHash)) throw IllegalArgumentException("BAD_CREDENTIALS")
        val access = jwt.createAccessToken(u.id!!)
        val refresh = jwt.createRefreshToken(u.id!!)
        val sess = UserSession(userId = u.id!!, refreshTokenHash = BCrypt.hashpw(refresh, BCrypt.gensalt()), expiresAt = Instant.now().plusSeconds(1209600))
        sessions.save(sess)
        return Tokens(access, refresh)
    }
}
