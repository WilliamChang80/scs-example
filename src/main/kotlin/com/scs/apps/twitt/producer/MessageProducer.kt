package com.scs.apps.twitt.producer

import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.integration.support.MessageBuilder
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class MessageProducer(private val streamBridge: StreamBridge) {

//    private val log = LoggerFactory.getLogger(this.javaClass)

    fun sendMessage(payload: ByteArray, key: ByteArray, topic: String) {
        val message: Message<Any> = buildMessage(payload, key, topic)
        println("sent message with key: $key and value: $payload")
        streamBridge.send(topic, message);
    }

    private fun buildMessage(payload: Any, key: ByteArray, topic: String): Message<Any> {
        return MessageBuilder
            .withPayload(payload)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .setHeader(KafkaHeaders.MESSAGE_KEY, key)
            .build()
    }

}