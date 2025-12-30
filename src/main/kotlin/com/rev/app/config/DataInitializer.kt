package com.rev.app.config

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.auth.UserRole
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@Profile("!prod") // 프로덕션 환경에서는 실행하지 않음
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Bean
    fun initData(): CommandLineRunner {
        return CommandLineRunner {
            // 테스트 계정이 없으면 생성
            val existingUser = userRepository.findByEmail("test1@test.com")
            if (existingUser == null) {
                val testUser = UserEntity(
                    email = "test1@test.com",
                    username = "testuser1",
                    password = passwordEncoder.encode("00001234"),
                    role = UserRole.IDOL // IDOL 권한 부여
                )
                userRepository.save(testUser)
                println("✅ 테스트 계정 생성 완료: test1@test.com / 00001234 (IDOL 권한)")
            } else {
                // 기존 사용자가 있으면 IDOL 권한으로 업데이트
                if (existingUser.role != UserRole.IDOL) {
                    existingUser.role = UserRole.IDOL
                    userRepository.save(existingUser)
                    println("✅ 테스트 계정 권한 업데이트: test1@test.com -> IDOL")
                }
            }
        }
    }
}

