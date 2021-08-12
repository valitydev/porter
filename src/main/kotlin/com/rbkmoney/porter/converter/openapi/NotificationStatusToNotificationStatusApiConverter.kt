package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.porter.converter.NotificatorConverter
import com.rbkmoney.porter.repository.entity.NotificationStatus
import org.springframework.stereotype.Component

@Component
class NotificationStatusToNotificationStatusApiConverter :
    NotificatorConverter<NotificationStatus, com.rbkmoney.openapi.notification.model.NotificationStatus> {

    override fun convert(notificationStatus: NotificationStatus): com.rbkmoney.openapi.notification.model.NotificationStatus {
        return when (notificationStatus) {
            NotificationStatus.read -> com.rbkmoney.openapi.notification.model.NotificationStatus.READ
            NotificationStatus.unread -> com.rbkmoney.openapi.notification.model.NotificationStatus.UNREAD
        }
    }
}
