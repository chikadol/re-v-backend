package com.rev.app.domain.community.repo

import com.rev.app.domain.community.ViewLogDaily
import com.rev.app.domain.community.ViewLogId
import org.springframework.data.jpa.repository.JpaRepository

interface ViewLogDailyRepository : JpaRepository<ViewLogDaily, ViewLogId>
