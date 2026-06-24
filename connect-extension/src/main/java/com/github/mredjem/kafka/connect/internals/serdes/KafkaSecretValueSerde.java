package com.github.mredjem.kafka.connect.internals.serdes;

import com.github.mredjem.kafka.connect.internals.KafkaSecretValue;
import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaSecretValueSerde {

  private static final Gson GSON = new Gson();

  private KafkaSecretValueSerde() {}

  public static class KafkaSecretValueSerializer implements Serializer<KafkaSecretValue> {

    @Override
    public byte[] serialize(String topic, KafkaSecretValue data) {
      try {
        if (data == null) {
          return null;
        }

        return GSON.toJson(data).getBytes();

      } catch (final Exception e) {
        throw new DeSerializationException("serialize", false, e);
      }
    }
  }

  public static class KafkaSecretValueDeserializer implements Deserializer<KafkaSecretValue> {

    @Override
    public KafkaSecretValue deserialize(String topic, byte[] data) {
      try {
        if (data == null) {
          return null;
        }

        return GSON.fromJson(new String(data), KafkaSecretValue.class);

      } catch (final Exception e) {
        throw new DeSerializationException("deserialize", false, e);
      }
    }
  }
}
