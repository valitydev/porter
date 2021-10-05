package com.rbkmoney.porter.listener.handler.party

import com.rbkmoney.damsel.payment_processing.PartyChange
import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.porter.listener.constant.HandleEventType
import com.rbkmoney.porter.listener.handler.ChangeHandler
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PartyCreateHandler(
    private val partyRepository: PartyRepository,
) : ChangeHandler<PartyChange, MachineEvent> {

    override fun handleChange(change: PartyChange, parent: MachineEvent) {
        val partyCreated = change.partyCreated
        val partyCreatedAt = TypeUtil.stringToLocalDateTime(partyCreated.createdAt)
        val partyEntity = partyRepository.findByPartyId(partyCreated.id) ?: PartyEntity()
        partyEntity.apply {
            partyId = partyCreated.id
            createdAt = partyCreatedAt
            email = partyCreated.contact_info.email
            status = PartyStatus.active
        }
        log.info { "Save party entity on create event: $partyEntity" }
        partyRepository.save(partyEntity)
    }

    override val changeType: HandleEventType = HandleEventType.PARTY_CREATED
}
