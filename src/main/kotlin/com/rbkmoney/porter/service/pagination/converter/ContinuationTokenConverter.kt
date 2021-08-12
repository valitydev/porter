package com.rbkmoney.porter.service.pagination.converter

import com.rbkmoney.porter.service.pagination.ContinuationToken

interface ContinuationTokenConverter {

    fun fromString(token: String): ContinuationToken

    fun toString(token: ContinuationToken): String
}
