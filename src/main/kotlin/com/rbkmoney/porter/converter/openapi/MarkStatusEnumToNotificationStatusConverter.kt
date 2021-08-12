package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.openapi.notification.model.MarkNotifications
import com.rbkmoney.porter.converter.NotificatorConverter
import com.rbkmoney.porter.repository.entity.NotificationStatus
import org.springframework.stereotype.Component

@Component
class MarkStatusEnumToNotificationStatusConverter :
    NotificatorConverter<MarkNotifications.StatusEnum, com.rbkmoney.porter.repository.entity.NotificationStatus> {

    override fun convert(status: MarkNotifications.StatusEnum): NotificationStatus {
        return when (status) {
            MarkNotifications.StatusEnum.READ -> com.rbkmoney.porter.repository.entity.NotificationStatus.read
            MarkNotifications.StatusEnum.UNREAD -> com.rbkmoney.porter.repository.entity.NotificationStatus.unread
        }
    }
}
