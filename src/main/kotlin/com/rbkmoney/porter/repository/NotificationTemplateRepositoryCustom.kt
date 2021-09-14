package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.model.DateFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.Page

interface NotificationTemplateRepositoryCustom {

    fun findNotificationTemplates(
        createdAt: DateFilter? = null,
        sentAt: DateFilter? = null,
        title: String? = null,
        content: String? = null,
        limit: Int = 10,
    ): Page<NotificationTemplateEntity>

    fun findNextNotificationTemplates(
        continuationToken: ContinuationToken,
        limit: Int = 10,
    ): Page<NotificationTemplateEntity>
}
