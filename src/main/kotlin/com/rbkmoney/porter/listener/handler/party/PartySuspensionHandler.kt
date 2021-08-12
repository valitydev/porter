package com.rbkmoney.porter.listener.handler.party

import com.rbkmoney.damsel.payment_processing.PartyChange
import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.porter.listener.constant.HandleEventType
import com.rbkmoney.porter.listener.handler.ChangeHandler
import com.rbkmoney.porter.listener.handler.merge.PartyMerger
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PartySuspensionHandler(
    private val partyRepository: PartyRepository,
    private val partyMerger: PartyMerger,
) : ChangeHandler<PartyChange, MachineEvent> {

    override fun handleChange(change: PartyChange, event: MachineEvent) {
        val partySuspension = change.partySuspension
        val partyId = event.sourceId
        val partyEntity = partyRepository.findByPartyId(partyId) ?: PartyEntity()
        val updateParty = partyEntity.apply {
            this.partyId = partyId
            if (partySuspension.isSetSuspended) {
                status = PartyStatus.suspended
            } else if (partySuspension.isSetActive) {
                status = PartyStatus.active
            }
        }
        partyMerger.mergeEvent(partyEntity, updateParty)
        log.info { "Save party entity on suspension event: $partyEntity" }
        partyRepository.save(updateParty)
    }

    override val changeType: HandleEventType = HandleEventType.PARTY_SUSPENSION
}
