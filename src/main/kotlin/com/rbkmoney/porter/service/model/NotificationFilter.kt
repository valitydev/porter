package com.rbkmoney.porter.service.model

import com.rbkmoney.porter.repository.entity.NotificationStatus
import java.time.LocalDateTime

data class NotificationFilter(
    val partyId: String? = null,
    val email: String? = null,
    val templateId: String? = null,
    val deleted: Boolean? = null,
    val status: NotificationStatus? = null,
    val fromTime: LocalDateTime? = null,
    val toTime: LocalDateTime? = null,
    val title: String? = null,
)
