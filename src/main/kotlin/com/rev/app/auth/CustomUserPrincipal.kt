package com.rev.app.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class CustomUserPrincipal(
    val userId: UUID,
    private val usernameValue: String,
    private val passwordValue: String,
    private val authoritiesValue: Collection<GrantedAuthority> = emptyList()
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authoritiesValue
    override fun getPassword(): String = passwordValue
    override fun getUsername(): String = usernameValue
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
