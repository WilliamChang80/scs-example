package com.scs.apps.twitt.serde;

import com.google.protobuf.MessageLite;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class Test<T extends MessageLite> extends Serdes.WrapperSerde<T> {

  public Test(Serializer<T> serializer, Deserializer<T> deserializer) {
    super(serializer, deserializer);
  }
}
