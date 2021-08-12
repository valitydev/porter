package com.rbkmoney.porter.converter.model

import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity

data class NotificationTemplateEntityEnriched(
    val notificationTemplateEntity: NotificationTemplateEntity,
    val readCount: Long? = null,
    val totalCount: Long? = null,
)
