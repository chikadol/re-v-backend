package com.rev.app.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class CustomUserDetails(private val user: UserEntity): UserDetails {
    override fun getUsername() = user.email
    override fun getPassword() = user.password
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        user.roles.split(',').map { SimpleGrantedAuthority("ROLE_" + it.trim()) }.toMutableList()
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
    fun id() = user.id
}