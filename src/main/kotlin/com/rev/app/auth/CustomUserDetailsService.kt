package com.rev.app.auth

import com.rev.app.auth.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // 엔티티에 roles가 없어도 동작하도록 기본 권한만 부여
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

        // 엔티티 필드명이 다르면 아래 두 줄만 맞춰 주세요.
        val loginId = user.username        // 예: user.loginId 라면 여기를 변경
        val password = user.password       // 예: user.passwordHash 라면 여기를 변경

        return CustomUserDetails(
            username = loginId,
            password = password,
            authorities0 = authorities
        )
    }
}
