package com.scs.apps.twitt.serde

import com.google.protobuf.MessageLite
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes.WrapperSerde
import org.apache.kafka.common.serialization.Serializer

class Test<T : MessageLite?>(serializer: Serializer<T>?, deserializer: Deserializer<T>?) :
    WrapperSerde<T>(serializer, deserializer)