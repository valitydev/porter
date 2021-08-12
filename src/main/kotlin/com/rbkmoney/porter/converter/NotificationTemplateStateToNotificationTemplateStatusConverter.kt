package com.rbkmoney.porter.converter

import com.rbkmoney.notification.NotificationTemplateState
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import org.springframework.stereotype.Component

@Component
class NotificationTemplateStateToNotificationTemplateStatusConverter :
    NotificatorConverter<NotificationTemplateState, NotificationTemplateStatus> {

    override fun convert(notificationTemplateState: NotificationTemplateState): NotificationTemplateStatus {
        return when (notificationTemplateState) {
            NotificationTemplateState.draft_state -> NotificationTemplateStatus.draft
            NotificationTemplateState.final_state -> NotificationTemplateStatus.final
        }
    }
}
