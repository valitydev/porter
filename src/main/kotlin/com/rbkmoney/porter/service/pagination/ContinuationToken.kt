package com.rbkmoney.porter.service.pagination

data class ContinuationToken(
    val keyParams: Map<String, String>?,
    val timestamp: Long,
    val id: String,
)
