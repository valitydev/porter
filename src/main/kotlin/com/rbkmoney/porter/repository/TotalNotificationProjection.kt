package com.rbkmoney.porter.repository

interface TotalNotificationProjection {
    val total: Long
    val read: Long
}
