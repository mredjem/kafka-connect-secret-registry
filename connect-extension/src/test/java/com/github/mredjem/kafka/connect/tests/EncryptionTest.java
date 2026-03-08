package com.github.mredjem.kafka.connect.tests;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.internals.KafkaSecretValue;
import com.github.mredjem.kafka.connect.internals.mappers.KafkaSecretValueMapper;
import com.github.mredjem.kafka.connect.internals.serdes.KafkaSecretValueSerde;
import com.github.mredjem.kafka.connect.utils.EncryptionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EncryptionTest {

  @SuppressWarnings("deprecation")
  private static final String MASTER_KEY = RandomStringUtils.randomAlphanumeric(40);

  @Test
  void encryptedSecretShouldBeDecrypted() {
    String secret = "Password123*";

    EncryptedSecret encrypted = EncryptionUtils.encrypt(secret, MASTER_KEY);

    byte[] decryptedSecret = EncryptionUtils.decrypt(encrypted, MASTER_KEY);

    Assertions.assertNotEquals(secret, new String(encrypted.getEncryptedSecret()));
    Assertions.assertEquals(secret, new String(decryptedSecret));
  }

  @Test
  void saltShouldBeStoredAlongsideCipherText() {
    String secret = "Password123*";

    KafkaSecretValue kafkaSecretValue = KafkaSecretValueMapper
      .create(MASTER_KEY)
      .newValue("com.github.mredjem.kafka.connect", "secret.registry", 1, secret);

    byte[] serialized = serialize(kafkaSecretValue);

    EncryptedSecret encrypted = encryptedSecret(deserialize(serialized));

    byte[] decrypted = EncryptionUtils.decrypt(encrypted, MASTER_KEY);

    Assertions.assertEquals(secret, new String(decrypted));
    Assertions.assertEquals(EncryptionUtils.checksum(new String(decrypted)), kafkaSecretValue.getChecksum());
  }

  private static EncryptedSecret encryptedSecret(KafkaSecretValue kafkaSecretValue) {
    EncryptedSecret encryptedSecret = new EncryptedSecret();

    encryptedSecret.setEncryptedSecret(kafkaSecretValue.getEncrypted().getContent());
    encryptedSecret.setSalt(kafkaSecretValue.getEncrypted().getSalt());

    return encryptedSecret;
  }

  private static byte[] serialize(KafkaSecretValue kafkaSecretValue) {
    try (KafkaSecretValueSerde.KafkaSecretValueSerializer serializer = new KafkaSecretValueSerde.KafkaSecretValueSerializer()) {
      return serializer.serialize("_connect-secrets", kafkaSecretValue);
    }
  }

  private static KafkaSecretValue deserialize(byte[] bytes) {
    try (KafkaSecretValueSerde.KafkaSecretValueDeserializer deserializer = new KafkaSecretValueSerde.KafkaSecretValueDeserializer()) {
      return deserializer.deserialize("_connect-secrets", bytes);
    }
  }
}
