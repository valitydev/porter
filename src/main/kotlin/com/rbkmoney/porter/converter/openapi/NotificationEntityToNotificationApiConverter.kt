package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.openapi.notification.model.Notification
import com.rbkmoney.openapi.notification.model.NotificationStatus
import com.rbkmoney.porter.converter.NotificatorConverter
import com.rbkmoney.porter.repository.entity.NotificationEntity
import org.springframework.context.annotation.Lazy
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.UUID

@Component
class NotificationEntityToNotificationApiConverter(
    @Lazy private val conversionService: ConversionService,
) : NotificatorConverter<NotificationEntity, Notification> {

    override fun convert(notificationEntity: NotificationEntity): Notification {
        return Notification().apply {
            id = UUID.fromString(notificationEntity.notificationId)
            createdAt = notificationEntity.createdAt.atOffset(ZoneOffset.UTC)
            status = conversionService.convert(notificationEntity.status, NotificationStatus::class.java)
            title = notificationEntity.notificationTemplateEntity!!.title
            content = notificationEntity.notificationTemplateEntity!!.content
        }
    }
}
