package com.rev.app.auth


import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class CustomUserDetailsService(private val userRepo: UserRepository) {
    fun load(username: String): CustomUserDetails {
        val user = userRepo.findByEmail(username) ?: throw UsernameNotFoundException(username)
        return CustomUserDetails(user)
    }
}