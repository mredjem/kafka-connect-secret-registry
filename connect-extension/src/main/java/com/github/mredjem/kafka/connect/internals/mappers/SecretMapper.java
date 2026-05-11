package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.Path;
import com.github.mredjem.kafka.connect.Secret;
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

    return Path.of(kafkaSecretValue.getPath())
      .key(kafkaSecretValue.getKey())
      .version(kafkaSecretValue.getVersion())
      .secret(new String(decrypted));
  }
}
