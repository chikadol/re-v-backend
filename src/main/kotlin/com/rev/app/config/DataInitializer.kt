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
            // 테스트 계정 (IDOL 권한) 생성
            val existingUser = userRepository.findByEmail("test1@test.com")
            if (existingUser == null) {
                val testUser = UserEntity(
                    email = "test1@test.com",
                    username = "testuser1",
                    password = passwordEncoder.encode("00001234"),
                    role = UserRole.IDOL
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

            // 어드민 계정 생성
            val existingAdmin = userRepository.findByEmail("admin@rev.com")
            if (existingAdmin == null) {
                val adminUser = UserEntity(
                    email = "admin@rev.com",
                    username = "admin",
                    password = passwordEncoder.encode("admin1234"),
                    role = UserRole.ADMIN
                )
                userRepository.save(adminUser)
                println("✅ 어드민 계정 생성 완료: admin@rev.com / admin1234 (ADMIN 권한)")
            } else {
                // 기존 어드민이 있으면 ADMIN 권한으로 업데이트
                if (existingAdmin.role != UserRole.ADMIN) {
                    existingAdmin.role = UserRole.ADMIN
                    userRepository.save(existingAdmin)
                    println("✅ 어드민 계정 권한 업데이트: admin@rev.com -> ADMIN")
                }
            }
        }
    }
}

