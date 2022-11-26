package com.scs.apps.twitt.serde

import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import lombok.NoArgsConstructor
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes.WrapperSerde
import org.apache.kafka.common.serialization.Serializer

class ProtobufSerde<T : MessageLite>(parser: Parser<T>) :
    WrapperSerde<T>(ProtobufSerializer(), ProtobufDeserializer(parser)) {

    @NoArgsConstructor
    class ProtobufDeserializer<T : MessageLite>(private val parser: Parser<T>) : Deserializer<T> {
        override fun deserialize(topic: String, data: ByteArray): T {
            return try {
                parser.parseFrom(data)
            } catch (e: InvalidProtocolBufferException) {
                throw SerializationException("Error deserializing proto", e)
            }
        }
    }

    @NoArgsConstructor
    class ProtobufSerializer<T : MessageLite> : Serializer<T> {
        override fun serialize(topic: String, data: T): ByteArray {
            return data.toByteArray()
        }
    }
}