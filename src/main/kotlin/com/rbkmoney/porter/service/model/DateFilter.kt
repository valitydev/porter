package com.rbkmoney.porter.service.model

import java.time.LocalDateTime

data class DateFilter(
    val from: LocalDateTime,
    val to: LocalDateTime
)
