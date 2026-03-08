package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.Secret;
import com.github.mredjem.kafka.connect.internals.KafkaSecretValue;
import com.github.mredjem.kafka.connect.utils.EncryptionUtils;

public class SecretMapper {

  private final String masterKey;

  private SecretMapper(String masterKey) {
    this.masterKey = masterKey;
  }

  public static SecretMapper create(String masterKey) {
    return new SecretMapper(masterKey);
  }

  public Secret newSecret(KafkaSecretValue kafkaSecretValue) {
    EncryptedSecret encryptedSecret = EncryptedSecretMapper.newEncryptedSecret(kafkaSecretValue.getEncrypted());

    byte[] decrypted = EncryptionUtils.decrypt(encryptedSecret, this.masterKey);

    return Secret.of(
      kafkaSecretValue.getPath(),
      kafkaSecretValue.getKey(),
      kafkaSecretValue.getVersion(),
      new String(decrypted)
    );
  }
}
