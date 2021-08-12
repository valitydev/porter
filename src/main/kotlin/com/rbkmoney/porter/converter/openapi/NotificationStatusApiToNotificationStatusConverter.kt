package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.openapi.notification.model.NotificationStatus
import com.rbkmoney.porter.converter.NotificatorConverter
import org.springframework.stereotype.Component

@Component
class NotificationStatusApiToNotificationStatusConverter :
    NotificatorConverter<NotificationStatus, com.rbkmoney.porter.repository.entity.NotificationStatus> {

    override fun convert(status: NotificationStatus): com.rbkmoney.porter.repository.entity.NotificationStatus {
        return when (status) {
            NotificationStatus.READ -> com.rbkmoney.porter.repository.entity.NotificationStatus.read
            NotificationStatus.UNREAD -> com.rbkmoney.porter.repository.entity.NotificationStatus.unread
        }
    }
}
