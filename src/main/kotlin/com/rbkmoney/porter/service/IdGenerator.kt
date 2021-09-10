package com.rbkmoney.porter.service

import java.util.UUID

object IdGenerator {
    fun randomString(): String {
        return UUID.randomUUID().toString()
    }
}
