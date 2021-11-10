package com.rbkmoney.porter.converter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.NotificationStatus
import com.rbkmoney.notification.Party
import com.rbkmoney.notification.PartyNotification
import com.rbkmoney.porter.repository.entity.NotificationEntity
import org.springframework.context.annotation.Lazy
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component

@Component
class NotificationEntityToPartyNotificationConverter(
    @Lazy private val conversionService: ConversionService,
) : NotificatorConverter<NotificationEntity, PartyNotification> {

    override fun convert(notificationEntity: NotificationEntity): PartyNotification {
        return PartyNotification().apply {
            templateId = notificationEntity.notificationTemplateEntity?.templateId
            party = Party(notificationEntity.partyEntity?.partyId, notificationEntity.partyEntity?.email)
            status = conversionService.convert(notificationEntity.status, NotificationStatus::class.java)
            createdAt = TypeUtil.temporalToString(notificationEntity.createdAt)
            deleted = notificationEntity.deleted
        }
    }
}
