package com.rbkmoney.porter.service.pagination

interface PageableEntity<T> {
    val id: T?
    val timestamp: Long
}
