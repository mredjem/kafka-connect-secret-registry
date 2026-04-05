package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.internals.KafkaSecretEncrypted;
import com.github.mredjem.kafka.connect.internals.KafkaSecretValue;
import com.github.mredjem.kafka.connect.internals.utils.EncryptionUtils;

import java.time.Instant;

public class KafkaSecretValueMapper {

  private final KafkaSecretEncryptedMapper kafkaSecretEncryptedMapper;

  private KafkaSecretValueMapper(String masterKey) {
    this.kafkaSecretEncryptedMapper = KafkaSecretEncryptedMapper.create(masterKey);
  }

  public static KafkaSecretValueMapper create(String masterKey) {
    return new KafkaSecretValueMapper(masterKey);
  }

  public KafkaSecretValue newValue(String path, String key, int version, String secret) {
    KafkaSecretValue kafkaSecretValue = new KafkaSecretValue();

    KafkaSecretEncrypted encrypted = this.kafkaSecretEncryptedMapper.newEncrypted(path, key, secret);

    kafkaSecretValue.setPath(path);
    kafkaSecretValue.setKey(key);
    kafkaSecretValue.setVersion(version);
    kafkaSecretValue.setEncrypted(encrypted);
    kafkaSecretValue.setChecksum(EncryptionUtils.checksum(secret));
    kafkaSecretValue.setCreatedBy(null);
    kafkaSecretValue.setCreatedAt(Instant.now().toEpochMilli());

    return kafkaSecretValue;
  }
}
