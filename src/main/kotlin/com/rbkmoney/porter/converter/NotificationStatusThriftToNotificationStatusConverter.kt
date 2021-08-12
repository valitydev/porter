package com.rbkmoney.porter.converter

import com.rbkmoney.notification.NotificationStatus
import org.springframework.stereotype.Component

@Component
class NotificationStatusThriftToNotificationStatusConverter :
    NotificatorConverter<NotificationStatus, com.rbkmoney.porter.repository.entity.NotificationStatus> {

    override fun convert(notificationStatus: NotificationStatus): com.rbkmoney.porter.repository.entity.NotificationStatus {
        return when (notificationStatus) {
            NotificationStatus.unread -> com.rbkmoney.porter.repository.entity.NotificationStatus.unread
            NotificationStatus.read -> com.rbkmoney.porter.repository.entity.NotificationStatus.read
        }
    }
}
