package com.rbkmoney.porter.listener.handler.merge

import org.springframework.beans.BeanWrapper
import org.springframework.beans.BeanWrapperImpl

abstract class BaseEventMerger<T> : EventMerger<T> {

    protected fun getNullPropertyNames(source: Any): Array<String> {
        val src: BeanWrapper = BeanWrapperImpl(source)
        val pds = src.propertyDescriptors
        val emptyNames: MutableSet<String> = HashSet()
        for (pd in pds) {
            src.getPropertyValue(pd.name) ?: emptyNames.add(pd.name)
        }
        return emptyNames.toTypedArray()
    }
}
