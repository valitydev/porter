package com.rbkmoney.porter.listener.handler.merge

import com.rbkmoney.porter.repository.entity.PartyEntity
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Component

@Component
class PartyMerger : BaseEventMerger<PartyEntity>() {

    override fun mergeEvent(source: PartyEntity, target: PartyEntity) {
        BeanUtils.copyProperties(source, target, *getNullPropertyNames(source))
    }
}
