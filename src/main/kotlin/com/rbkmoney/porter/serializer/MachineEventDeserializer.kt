package com.rbkmoney.porter.serializer

import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.machinegun.eventsink.SinkEvent
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Deserializer
import org.apache.thrift.TDeserializer
import org.apache.thrift.protocol.TBinaryProtocol

private val log = KotlinLogging.logger {}

class MachineEventDeserializer : Deserializer<MachineEvent> {

    private val thriftDeserializerThreadLocal: ThreadLocal<TDeserializer> = ThreadLocal.withInitial {
        TDeserializer(TBinaryProtocol.Factory())
    }

    override fun deserialize(topic: String, data: ByteArray): MachineEvent {
        log.debug("Message, topic: {}, byteLength: {}", topic, data.size)
        val machineEvent = SinkEvent()
        try {
            thriftDeserializerThreadLocal.get().deserialize(machineEvent, data)
        } catch (e: Exception) {
            log.error("Error when deserialize data: {} ", data, e)
        }
        return machineEvent.event
    }
}
