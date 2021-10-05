package com.rbkmoney.porter.service

import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationSenderService(
    private val notificationTemplateService: NotificationTemplateService,
    private val notificationService: NotificationService,
) {

    @Transactional
    fun sendNotification(templateId: String, partyIds: Collection<String>) {
        notificationTemplateService.editNotificationTemplate(
            templateId = templateId,
            status = NotificationTemplateStatus.final
        )
        notificationService.createNotifications(templateId, partyIds)
    }

    @Transactional
    fun sendNotificationAll(templateId: String) {
        notificationTemplateService.editNotificationTemplate(
            templateId = templateId,
            status = NotificationTemplateStatus.final
        )
        notificationService.createNotifications(templateId)
    }
}
