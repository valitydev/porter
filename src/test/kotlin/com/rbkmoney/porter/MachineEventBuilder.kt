package com.rbkmoney.porter

import com.rbkmoney.damsel.domain.Blocked
import com.rbkmoney.damsel.domain.Blocking
import com.rbkmoney.damsel.domain.PartyContactInfo
import com.rbkmoney.damsel.domain.Suspended
import com.rbkmoney.damsel.domain.Suspension
import com.rbkmoney.damsel.payment_processing.PartyChange
import com.rbkmoney.damsel.payment_processing.PartyCreated
import com.rbkmoney.damsel.payment_processing.PartyEventData
import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.machinegun.eventsink.SinkEvent
import com.rbkmoney.machinegun.msgpack.Value
import com.rbkmoney.sink.common.serialization.impl.PartyEventDataSerializer
import org.jeasy.random.EasyRandom
import java.time.Instant
import java.time.LocalDateTime

object MachineEventBuilder {

    private val easyRandom = EasyRandom()

    // Party Create

    fun buildMessagePartyCreated(sequenceId: Long, partyId: String): MachineEvent {
        val partyChange = buildPartyCreatedPartyChange(partyId)
        return buildMachineEvent(partyId, sequenceId, partyChange)
    }

    fun buildPartyCreatedPartyChange(partyId: String): PartyChange {
        return PartyChange().apply {
            this.partyCreated = buildPartyCreated(partyId)
        }
    }

    fun buildPartyCreated(partyId: String): PartyCreated {
        return PartyCreated(
            partyId,
            PartyContactInfo(easyRandom.nextObject(String::class.java)),
            TypeUtil.temporalToString(LocalDateTime.now())
        )
    }

    // Party Block

    fun buildMessagePartyBlocking(sequenceId: Long, partyId: String): MachineEvent {
        val partyChange: PartyChange = buildPartyBlockingPartyChange()
        return buildMachineEvent(partyId, sequenceId, partyChange)
    }

    fun buildPartyBlockingPartyChange(): PartyChange {
        return PartyChange().apply {
            this.partyBlocking = buildPartyBlocking()
        }
    }

    fun buildPartyBlocking(): Blocking {
        val blocking = Blocking()
        blocking.blocked = Blocked(
            easyRandom.nextObject(String::class.java),
            TypeUtil.temporalToString(LocalDateTime.now())
        )
        return blocking
    }

    // Party suspended

    fun buildMessagePartySuspension(sequenceId: Long, partyId: String): MachineEvent {
        val partyChange = buildPartySuspensionPartyChange()
        return buildMachineEvent(partyId, sequenceId, partyChange)
    }

    fun buildPartySuspensionPartyChange(): PartyChange {
        val suspension = buildPartySuspension()
        return PartyChange().apply {
            partySuspension = suspension
        }
    }

    fun buildPartySuspension(): Suspension {
        return Suspension().apply {
            suspended = Suspended(TypeUtil.temporalToString(LocalDateTime.now()))
        }
    }

    fun buildMachineEvent(sourceId: String, sequenceId: Long, vararg partyChange: PartyChange): MachineEvent {
        val message = MachineEvent()
        message.createdAt = TypeUtil.temporalToString(Instant.now())
        message.eventId = sequenceId
        message.sourceNs = "sourceNs"
        message.sourceId = sourceId
        val data = Value().apply {
            bin = PartyEventDataSerializer().serialize(PartyEventData(partyChange.toList()))
        }
        message.setData(data)
        return message
    }

    fun buildSinkEvent(machineEvent: MachineEvent): SinkEvent {
        val sinkEvent = SinkEvent()
        sinkEvent.event = machineEvent
        return sinkEvent
    }
}
