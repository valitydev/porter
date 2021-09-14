package com.rbkmoney.porter.service.model

data class NotificationTemplateFilter(
    val title: String? = null,
    val content: String? = null,
    val createdDateFilter: DateFilter? = null,
    val sentDateFilter: DateFilter? = null,
)
