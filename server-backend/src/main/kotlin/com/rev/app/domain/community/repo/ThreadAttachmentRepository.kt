package com.rev.app.domain.community.repo

import com.rev.app.domain.community.ThreadAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadAttachmentRepository : JpaRepository<ThreadAttachment, Long>
