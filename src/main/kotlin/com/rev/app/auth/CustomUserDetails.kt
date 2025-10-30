package com.rev.app.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val username: String,
    private val password: String,
    private val authorities0: Collection<GrantedAuthority>
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities0
    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}
