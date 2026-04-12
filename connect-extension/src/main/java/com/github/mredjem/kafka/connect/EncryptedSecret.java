package com.github.mredjem.kafka.connect;

public class EncryptedSecret {

  private final byte[] encryptedSecret;

  private final byte[] salt;

  private EncryptedSecret(byte[] encryptedSecret, byte[] salt) {
    this.encryptedSecret = encryptedSecret;
    this.salt = salt;
  }

  public static EncryptedSecret of(byte[] encryptedSecret, byte[] salt) {
    return new EncryptedSecret(encryptedSecret, salt);
  }

  public byte[] getEncryptedSecret() {
    return this.encryptedSecret;
  }

  public byte[] getSalt() {
    return this.salt;
  }
}
