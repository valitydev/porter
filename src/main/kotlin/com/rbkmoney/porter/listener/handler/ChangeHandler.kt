package com.rbkmoney.porter.listener.handler

import com.rbkmoney.porter.listener.constant.HandleEventType
import org.apache.thrift.TBase

interface ChangeHandler<in C : TBase<*, *>, P> {

    fun accept(change: C): Boolean {
        return changeType?.filter?.match(change) ?: false
    }

    fun handleChange(change: C, parent: P)

    val changeType: HandleEventType?
        get() = null
}
