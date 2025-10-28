package com.rev.app.auth

import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Bean
fun userDetailsService(users: UserRepository): org.springframework.security.core.userdetails.UserDetailsService =
    org.springframework.security.core.userdetails.UserDetailsService { username ->
        val u = users.findByEmail(username) ?: throw UsernameNotFoundException(username)
        CustomUserDetails(u)
    }