package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.Page

interface NotificationRepositoryCustom {

    fun findNotifications(filter: NotificationFilter?, limit: Int = 10): Page<NotificationEntity>

    fun findNextNotifications(continuationToken: ContinuationToken, limit: Int = 10): Page<NotificationEntity>
}
