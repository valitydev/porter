package com.rbkmoney.porter.converter

import com.rbkmoney.notification.NotificationStatus
import org.springframework.stereotype.Component

@Component
class NotificationStatusToNotificationStatusThriftConverter :
    NotificatorConverter<com.rbkmoney.porter.repository.entity.NotificationStatus, NotificationStatus> {

    override fun convert(notificationStatus: com.rbkmoney.porter.repository.entity.NotificationStatus): NotificationStatus {
        return when (notificationStatus) {
            com.rbkmoney.porter.repository.entity.NotificationStatus.unread -> NotificationStatus.unread
            com.rbkmoney.porter.repository.entity.NotificationStatus.read -> NotificationStatus.read
        }
    }
}
