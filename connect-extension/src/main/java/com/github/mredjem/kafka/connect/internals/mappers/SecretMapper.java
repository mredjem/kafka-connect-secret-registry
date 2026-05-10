package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.Secret;
import com.github.mredjem.kafka.connect.Version;
import com.github.mredjem.kafka.connect.internals.KafkaSecretEncrypted;
import com.github.mredjem.kafka.connect.internals.KafkaSecretValue;
import com.github.mredjem.kafka.connect.internals.utils.EncryptionUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class SecretMapper {

  private final String masterKey;

  public Secret newSecret(KafkaSecretValue kafkaSecretValue) {
    KafkaSecretEncrypted kafkaSecretEncrypted = kafkaSecretValue.getEncrypted();

    EncryptedSecret encryptedSecret = EncryptedSecret.of(
      kafkaSecretEncrypted.getContent(),
      kafkaSecretEncrypted.getSalt(),
      kafkaSecretEncrypted.getIv()
    );

    byte[] decrypted = EncryptionUtils.decrypt(encryptedSecret, this.masterKey);

    Version version = Version.of(
      kafkaSecretValue.getPath(),
      kafkaSecretValue.getKey(),
      kafkaSecretValue.getVersion()
    );

    return Secret.of(version, new String(decrypted));
  }
}
