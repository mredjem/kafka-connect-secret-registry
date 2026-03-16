package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.internals.KafkaSecretKey;

public class KafkaSecretKeyMapper {

  private KafkaSecretKeyMapper() {}

  public static KafkaSecretKey newKey(String path, String key, int version) {
    KafkaSecretKey kafkaSecretKey = new KafkaSecretKey();

    kafkaSecretKey.setPath(path);
    kafkaSecretKey.setKey(key);
    kafkaSecretKey.setVersion(version);

    return kafkaSecretKey;
  }
}
