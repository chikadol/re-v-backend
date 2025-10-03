package com.rev.app.api

import java.time.Instant

data class PageResp<T>(val items: List<T>, val nextCursor: String?)

data class ArtistItem(
    val id: Long,
    val stageName: String,
    val stageNameKr: String?,
    val groupName: String?,
    val avatarUrl: String?,
    val popularityScore: Int
)

data class GenbaItem(
    val id: Long,
    val title: String,
    val startAt: Instant,
    val endAt: Instant?,
    val areaCode: String?,
    val placeName: String?,
    val posterUrl: String?,
    val popularityScore: Int
)

data class SignupReq(val email: String, val password: String)
data class LoginReq(val email: String, val password: String)
