package com.rbkmoney.porter.converter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import org.springframework.context.annotation.Lazy
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component

@Component
class NotificationTemplateToNotificationTemplateEntityConverter(
    @Lazy private val conversionService: ConversionService,
) : NotificatorConverter<NotificationTemplate, NotificationTemplateEntity> {

    override fun convert(notificationTemplate: NotificationTemplate): NotificationTemplateEntity {
        return NotificationTemplateEntity().apply {
            templateId = notificationTemplate.templateId
            createdAt = TypeUtil.stringToLocalDateTime(notificationTemplate.createdAt)
            updatedAt = TypeUtil.stringToLocalDateTime(notificationTemplate.updatedAt)
            title = notificationTemplate.title
            content = notificationTemplate.content.text
            contentType = notificationTemplate.content.contentType
            status = conversionService.convert(notificationTemplate.state, NotificationTemplateStatus::class.java)
                ?: throw IllegalArgumentException("Unknown notification template status: ${notificationTemplate.state.name}")
        }
    }
}
