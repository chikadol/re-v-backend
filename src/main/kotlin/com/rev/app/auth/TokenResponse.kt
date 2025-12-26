package com.rev.app.auth

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
