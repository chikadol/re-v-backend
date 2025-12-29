package com.rev.app.config

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
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
            if (userRepository.findByEmail("test1@test.com") == null) {
                val testUser = UserEntity(
                    email = "test1@test.com",
                    username = "testuser1",
                    password = passwordEncoder.encode("00001234")
                )
                userRepository.save(testUser)
                println("✅ 테스트 계정 생성 완료: test1@test.com / 00001234")
            }
        }
    }
}

