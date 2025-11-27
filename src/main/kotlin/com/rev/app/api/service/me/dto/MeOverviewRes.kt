package com.rev.app.api.service.me.dto

data class MeOverviewRes(
    val threadCount: Long,
    val commentCount: Long,
    val bookmarkCount: Long,
    val unreadNotificationCount: Long
)
