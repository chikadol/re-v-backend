package com.rev.app.auth


import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class CustomUserDetailsService(private val userRepo: UserRepository): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepo.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return CustomUserDetails(user)
    }
}