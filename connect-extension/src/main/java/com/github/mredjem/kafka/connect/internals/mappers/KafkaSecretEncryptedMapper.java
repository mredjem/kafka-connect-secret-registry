package com.github.mredjem.kafka.connect.internals.mappers;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.internals.KafkaSecretEncrypted;
import com.github.mredjem.kafka.connect.internals.utils.EncryptionUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class KafkaSecretEncryptedMapper {

  private final String masterKey;

  public KafkaSecretEncrypted newEncrypted(String path, String key, String secret) {
    KafkaSecretEncrypted encrypted = new KafkaSecretEncrypted();

    EncryptedSecret encryptedSecret = EncryptionUtils.encrypt(secret, this.masterKey);

    encrypted.setDerivationInfo(path + "/" + key);
    encrypted.setContent(encryptedSecret.getSecret());
    encrypted.setSalt(encryptedSecret.getSalt());
    encrypted.setIv(encryptedSecret.getIv());

    return encrypted;
  }
}
