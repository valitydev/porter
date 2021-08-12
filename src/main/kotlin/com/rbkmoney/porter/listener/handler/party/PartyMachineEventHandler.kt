package com.rbkmoney.porter.listener.handler.party

import com.rbkmoney.damsel.payment_processing.PartyChange
import com.rbkmoney.damsel.payment_processing.PartyEventData
import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.porter.config.properties.KafkaProperties
import com.rbkmoney.porter.listener.handler.ChangeHandler
import com.rbkmoney.sink.common.parser.impl.MachineEventParser
import mu.KotlinLogging
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

private val log = KotlinLogging.logger {}

@Component
class PartyMachineEventHandler(
    private val eventParser: MachineEventParser<PartyEventData>,
    private val partyHandlers: List<ChangeHandler<PartyChange, MachineEvent>>,
    private val kafkaProperties: KafkaProperties,
) {

    @Transactional
    fun handleMessages(batch: List<MachineEvent>, ack: Acknowledgment) {
        try {
            if (CollectionUtils.isEmpty(batch)) return

            for (machineEvent in batch) {
                handleEvent(machineEvent)
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error(e) { "Exception during PartyListener process" }
            ack.nack(kafkaProperties.consumer.throttlingTimeoutMs.toLong())
            throw e
        }
    }

    private fun handleEvent(machineEvent: MachineEvent) {
        log.debug { "Party machine event: $machineEvent" }
        val eventData = eventParser.parse(machineEvent)
        if (eventData.isSetChanges) {
            log.info { "Party changes size: ${eventData.changes.size}" }
            for (change in eventData.getChanges()) {
                log.info { "Party change: $change" }
                partyHandlers.stream()
                    .filter {
                        it.accept(change)
                    }
                    .forEach {
                        it.handleChange(change, machineEvent)
                    }
            }
        }
    }
}
