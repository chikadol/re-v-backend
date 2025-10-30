import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByToken(token: String): RefreshTokenEntity?
    fun existsByToken(token: String): Boolean
}
