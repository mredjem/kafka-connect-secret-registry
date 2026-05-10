package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.internals.KafkaSecretKey;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaSecretKeyMapper {

  public KafkaSecretKey newKey(String path, String key, int version) {
    KafkaSecretKey kafkaSecretKey = new KafkaSecretKey();

    kafkaSecretKey.setPath(path);
    kafkaSecretKey.setKey(key);
    kafkaSecretKey.setVersion(version);

    return kafkaSecretKey;
  }
}
