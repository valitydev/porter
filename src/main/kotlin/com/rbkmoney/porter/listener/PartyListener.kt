package com.rbkmoney.porter.listener

import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.porter.listener.handler.party.PartyMachineEventHandler
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class PartyListener(
    private val partyMachineEventHandler: PartyMachineEventHandler,
) {

    @KafkaListener(
        autoStartup = "\${kafka.consumer.enabled}",
        topics = ["\${kafka.topic.initial}"],
        containerFactory = "partyListenerContainerFactory"
    )
    fun listen(
        batch: List<MachineEvent>,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.OFFSET) offsets: Int,
        ack: Acknowledgment,
    ) {
        log.info { "PartyListener listen offsets=$offsets; partition=$partition; batchSize=${batch.size}" }
        partyMachineEventHandler.handleMessages(batch, ack)
        log.info { "PartyListener batch has been commited, batch.size=${batch.size}" }
    }
}
