package com.rbkmoney.porter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.machinegun.eventsink.SinkEvent
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.PartyStatus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class PartyListenerTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var partyRepository: PartyRepository

    @BeforeEach
    internal fun setUp() {
        partyRepository.deleteAll()
    }

    @Test
    fun `test created party event`() {
        // Given
        val partyId = "testPartyId"
        val sequenceId = AtomicLong(0L)
        val partyChange = MachineEventBuilder.buildPartyCreatedPartyChange(partyId)
        val partyCreateEvent = MachineEventBuilder.buildMachineEvent(partyId, sequenceId.incrementAndGet(), partyChange)
        val partyEvents = listOf<SinkEvent>(
            MachineEventBuilder.buildSinkEvent(partyCreateEvent)
        ).forEach { sendMachineEvent(it) }

        // When
        val partyEntity = await.pollInterval(1, TimeUnit.SECONDS).atMost(60, TimeUnit.SECONDS).untilNotNull {
            val party = partyRepository.findByPartyId(partyId)
            party
        }

        // Then
        assertEquals(partyChange.partyCreated.id, partyEntity.partyId)
        assertEquals(partyChange.partyCreated.contactInfo.email, partyEntity.email)
        assertEquals(
            TypeUtil.stringToLocalDateTime(partyChange.partyCreated.createdAt).withNano(0),
            partyEntity.createdAt?.withNano(0)
        )
    }

    @Test
    fun `test blocked party event`() {
        // Given
        val partyId = "testPartyId"
        val sequenceId = AtomicLong(0L)
        val partyChange = MachineEventBuilder.buildPartyCreatedPartyChange(partyId)
        val partyCreateEvent = MachineEventBuilder.buildMachineEvent(partyId, sequenceId.incrementAndGet(), partyChange)
        val partyEvents = listOf<SinkEvent>(
            MachineEventBuilder.buildSinkEvent(partyCreateEvent),
            MachineEventBuilder.buildSinkEvent(
                MachineEventBuilder.buildMessagePartyBlocking(sequenceId.incrementAndGet(), partyId)
            )
        ).forEach { sendMachineEvent(it) }

        // When
        await.pollInterval(1, TimeUnit.SECONDS).atMost(60, TimeUnit.SECONDS).until {
            val party = partyRepository.findByPartyId(partyId)
            party?.status == PartyStatus.blocked
        }
        val party = partyRepository.findByPartyId(partyId)!!

        // Then
        assertTrue(party.status == PartyStatus.blocked)
        assertEquals(partyChange.partyCreated.id, party.partyId)
        assertEquals(partyChange.partyCreated.contactInfo.email, party.email)
        assertEquals(
            TypeUtil.stringToLocalDateTime(partyChange.partyCreated.createdAt).withNano(0),
            party.createdAt?.withNano(0)
        )
    }

    @Test
    fun `test suspended party event`() {
        // Given
        val partyId = "testPartyId"
        val sequenceId = AtomicLong(0L)
        val partyChange = MachineEventBuilder.buildPartyCreatedPartyChange(partyId)
        val partyCreateEvent = MachineEventBuilder.buildMachineEvent(partyId, sequenceId.incrementAndGet(), partyChange)
        val partyEvents = listOf<SinkEvent>(
            MachineEventBuilder.buildSinkEvent(partyCreateEvent),
            MachineEventBuilder.buildSinkEvent(
                MachineEventBuilder.buildMessagePartySuspension(sequenceId.incrementAndGet(), partyId)
            )
        ).forEach { sendMachineEvent(it) }

        // When
        await.pollInterval(1, TimeUnit.SECONDS).atMost(60, TimeUnit.SECONDS).until {
            val party = partyRepository.findByPartyId(partyId)
            party?.status == PartyStatus.suspended
        }
        val party = partyRepository.findByPartyId(partyId)!!

        // Then
        assertTrue(party.status == PartyStatus.suspended)
        assertEquals(partyChange.partyCreated.id, party.partyId)
        assertEquals(partyChange.partyCreated.contactInfo.email, party.email)
        assertEquals(
            TypeUtil.stringToLocalDateTime(partyChange.partyCreated.createdAt).withNano(0),
            party.createdAt?.withNano(0)
        )
    }
}
