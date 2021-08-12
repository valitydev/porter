package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.openapi.notification.model.MarkAllNotifications
import com.rbkmoney.porter.converter.NotificatorConverter
import com.rbkmoney.porter.repository.entity.NotificationStatus
import org.springframework.stereotype.Component

@Component
class MarkAllStatusEnumToNotificationStatusConverter :
    NotificatorConverter<MarkAllNotifications.StatusEnum, com.rbkmoney.porter.repository.entity.NotificationStatus> {

    override fun convert(status: MarkAllNotifications.StatusEnum): NotificationStatus {
        return when (status) {
            MarkAllNotifications.StatusEnum.READ -> com.rbkmoney.porter.repository.entity.NotificationStatus.read
            MarkAllNotifications.StatusEnum.UNREAD -> com.rbkmoney.porter.repository.entity.NotificationStatus.unread
        }
    }
}
