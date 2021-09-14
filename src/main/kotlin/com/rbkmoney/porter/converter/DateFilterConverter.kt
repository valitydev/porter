package com.rbkmoney.porter.converter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.porter.service.model.DateFilter
import org.springframework.stereotype.Component
import com.rbkmoney.notification.DateFilter as DateFilterThrift

@Component
class DateFilterConverter : NotificatorConverter<DateFilterThrift, DateFilter> {

    override fun convert(value: DateFilterThrift): DateFilter {
        return DateFilter(TypeUtil.stringToLocalDateTime(value.fromDate), TypeUtil.stringToLocalDateTime(value.toDate))
    }
}
