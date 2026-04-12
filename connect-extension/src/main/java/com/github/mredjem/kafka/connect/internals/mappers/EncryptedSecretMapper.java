package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.internals.KafkaSecretEncrypted;

public class EncryptedSecretMapper {

  private EncryptedSecretMapper() {}

  public static EncryptedSecret newEncryptedSecret(KafkaSecretEncrypted kafkaSecretEncrypted) {
    return EncryptedSecret.of(kafkaSecretEncrypted.getContent(), kafkaSecretEncrypted.getSalt());
  }
}
