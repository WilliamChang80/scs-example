package com.scs.apps.twitt.producer

import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KeyValue
import org.jetbrains.kotlin.com.google.common.collect.ImmutableList
import org.springframework.stereotype.Component
import java.util.*

@Component
class StreamsProducer {
    private val REPLICATION_FACTOR: Short = 5
    private val PARTITIONS: Short = 5
    private val bootstrapServer = "localhost:29092"

    fun <K, V> publish(topic: String, message: KeyValue<K, V>, keySerde: Serde<K>, valueSerde: Serde<V>) {
        return publish(topic, ImmutableList.of(message), keySerde, valueSerde)
    }

    private fun <K, V> publish(
        topic: String, messages: List<KeyValue<K, V>>, keySerde: Serde<K>, valueSerde: Serde<V>
    ) {
        val properties = Properties()
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = keySerde.serializer().javaClass
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = valueSerde.serializer().javaClass
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer

        try {
            Admin.create(properties).use { adminClient ->
                KafkaProducer<Any, Any>(properties).use { producer ->
                    val topics = ImmutableList.of(createTopic(topic))
                    adminClient.createTopics(topics)
                    messages.forEach { pair: KeyValue<K, V> ->
                        val producerRecord: ProducerRecord<Any, Any> = ProducerRecord(topic, pair.key, pair.value)
                        producer.send(producerRecord, getCallback())
                    }
                    println("Message produced to $topic")
                }
            }
        } catch (e: Exception) {
            println("Failed produce message, " + e.localizedMessage)
        }
    }

    private fun getCallback(): Callback {
        return Callback { metadata, exception ->
            if (exception != null) {
                println("Producing records encountered error: $exception")
            } else {
                println("Record produced - offset - ${metadata.offset()} timestamp - ${metadata.timestamp()}")
            }
        }
    }

    private fun createTopic(topicName: String): NewTopic {
        return NewTopic(topicName, PARTITIONS.toInt(), REPLICATION_FACTOR)
    }

}