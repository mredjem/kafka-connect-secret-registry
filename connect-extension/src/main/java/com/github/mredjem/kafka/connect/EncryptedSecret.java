package com.github.mredjem.kafka.connect;

public class EncryptedSecret {

  private final byte[] secret;

  private final byte[] salt;

  private final byte[] iv;

  private EncryptedSecret(byte[] secret, byte[] salt, byte[] iv) {
    this.secret = secret;
    this.salt = salt;
    this.iv = iv;
  }

  public static EncryptedSecret of(byte[] secret, byte[] salt, byte[] iv) {
    return new EncryptedSecret(secret, salt, iv);
  }

  public byte[] getSecret() {
    return this.secret;
  }

  public byte[] getSalt() {
    return this.salt;
  }

  public byte[] getIv() {
    return this.iv;
  }
}
