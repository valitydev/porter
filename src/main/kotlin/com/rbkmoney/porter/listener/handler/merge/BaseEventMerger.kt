package com.rbkmoney.porter.listener.handler.merge

import org.springframework.beans.BeanWrapper
import org.springframework.beans.BeanWrapperImpl
import java.util.Arrays
import java.util.stream.Collectors

abstract class BaseEventMerger<T> : EventMerger<T> {

    protected fun getNullPropertyNames(source: Any): Array<String?> {
        val src: BeanWrapper = BeanWrapperImpl(source)
        val pds = src.propertyDescriptors
        val emptyNames = Arrays.stream(pds).map { src.getPropertyValue(it.name) }.collect(Collectors.toSet())
        return arrayOfNulls<String>(emptyNames.size)
    }
}
