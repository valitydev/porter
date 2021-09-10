package com.rbkmoney.porter.converter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.PartyNotificationRequest
import com.rbkmoney.porter.service.model.NotificationFilter
import org.springframework.stereotype.Component

@Component
class PartyNotificationRequestToNotificationFilterConverter :
    NotificatorConverter<PartyNotificationRequest, NotificationFilter> {

    override fun convert(value: PartyNotificationRequest): NotificationFilter {
        val fromTime = if (value.isSetDateFilter) TypeUtil.stringToLocalDateTime(value.dateFilter.fromDate) else null
        val toTime = if (value.isSetDateFilter) TypeUtil.stringToLocalDateTime(value.dateFilter.toDate) else null
        if (value.partyFilter.isSetPartyId) {
            return NotificationFilter(
                partyId = value.partyFilter.partyId,
                fromTime = fromTime,
                toTime = toTime
            )
        }
        return NotificationFilter(
            partyId = value.party_filter.partyId
        )
    }
}
