package com.github.mredjem.kafka.connect;

public class EncryptedSecret {

  private final byte[] secret;

  private final byte[] salt;

  private EncryptedSecret(byte[] secret, byte[] salt) {
    this.secret = secret;
    this.salt = salt;
  }

  public static EncryptedSecret of(byte[] secret, byte[] salt) {
    return new EncryptedSecret(secret, salt);
  }

  public byte[] getSecret() {
    return this.secret;
  }

  public byte[] getSalt() {
    return this.salt;
  }
}
