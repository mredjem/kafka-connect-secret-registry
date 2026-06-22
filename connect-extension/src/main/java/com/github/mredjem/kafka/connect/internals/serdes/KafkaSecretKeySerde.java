package com.github.mredjem.kafka.connect.internals.serdes;

import com.github.mredjem.kafka.connect.internals.KafkaSecretKey;
import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Objects;

public class KafkaSecretKeySerde {

  private static final Gson GSON = new Gson();

  private KafkaSecretKeySerde() {}

  public static class KafkaSecretKeySerializer implements Serializer<KafkaSecretKey> {

    @Override
    public byte[] serialize(String topic, KafkaSecretKey data) {
      try {
        Objects.requireNonNull(data);

        return GSON.toJson(data).getBytes();

      } catch (final Exception e) {
        throw new DeSerializationException("serialize", true, e);
      }
    }
  }

  public static class KafkaSecretKeyDeserializer implements Deserializer<KafkaSecretKey> {

    @Override
    public KafkaSecretKey deserialize(String topic, byte[] data) {
      try {
        Objects.requireNonNull(data);

        return GSON.fromJson(new String(data), KafkaSecretKey.class);

      } catch (final Exception e) {
        throw new DeSerializationException("deserialize", true, e);
      }
    }
  }
}
