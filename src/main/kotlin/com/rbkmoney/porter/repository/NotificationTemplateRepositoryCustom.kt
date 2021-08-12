package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.Page
import java.time.LocalDateTime

interface NotificationTemplateRepositoryCustom {

    fun findNotificationTemplates(
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        title: String? = null,
        content: String? = null,
        fixedDate: LocalDateTime? = null,
        limit: Int = 10,
    ): Page<NotificationTemplateEntity>

    fun findNextNotificationTemplates(
        continuationToken: ContinuationToken,
        limit: Int = 10,
    ): Page<NotificationTemplateEntity>
}
