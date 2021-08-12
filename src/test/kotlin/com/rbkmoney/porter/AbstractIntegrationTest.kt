package com.rbkmoney.porter

import com.rbkmoney.kafka.common.serialization.ThriftSerializer
import com.rbkmoney.machinegun.eventsink.SinkEvent
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.time.Duration
import java.util.Properties

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [PorterApplication::class], initializers = [AbstractIntegrationTest.Initializer::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AbstractIntegrationTest {

    @Value("\${kafka.topic.initial}")
    var sourceTopic: String? = null

    protected fun sendMachineEvent(event: SinkEvent) {
        createKafkaProducer().use {
            val producerRecord = ProducerRecord<String, SinkEvent>(sourceTopic, event.event.sourceId, event)
            it.send(producerRecord).get()
        }
    }

    private fun createKafkaProducer(): Producer<String, SinkEvent> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
        props[ProducerConfig.CLIENT_ID_CONFIG] = "testClientId"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ThriftSerializer::class.java.name

        return KafkaProducer(props)
    }

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            kafka.start()
            postgresql.start()
            TestPropertyValues.of(
                "spring.datasource.url=${postgresql.jdbcUrl}",
                "spring.datasource.username=${postgresql.username}",
                "spring.datasource.password=${postgresql.password}",
                "spring.jpa.show_sql=true",
                "kafka.bootstrap-servers=${kafka.bootstrapServers}",
            ).applyTo(applicationContext.environment)
        }

        private fun <T> initTopic(topicName: String, clazz: Class<*>): Consumer<String, T> {
            val consumer: Consumer<String, T> = createConsumer(clazz)
            consumer.subscribe(listOf(topicName))
            consumer.poll(Duration.ofMillis(500L))
            return consumer
        }

        private fun <T> createConsumer(clazz: Class<*>): Consumer<String, T> {
            val props = Properties()
            props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
            props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = clazz
            props[ConsumerConfig.GROUP_ID_CONFIG] = "test"
            props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            return KafkaConsumer(props)
        }
    }

    companion object {
        val postgresql = PostgreSQLContainer<Nothing>("postgres:12").apply {
            withDatabaseName("porter")
            withUsername("root")
            withPassword("password")
        }
        private val kafka: KafkaContainer by lazy {
            KafkaContainer().withEmbeddedZookeeper()
        }
    }
}
