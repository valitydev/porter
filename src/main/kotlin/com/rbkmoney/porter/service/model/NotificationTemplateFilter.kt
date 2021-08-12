package com.rbkmoney.porter.service.model

import java.time.LocalDateTime

data class NotificationTemplateFilter(
    val title: String? = null,
    val content: String? = null,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val date: LocalDateTime? = null,
)
