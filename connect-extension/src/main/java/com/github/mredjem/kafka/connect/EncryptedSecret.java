package com.github.mredjem.kafka.connect;

public class EncryptedSecret {

  private byte[] encryptedSecret;

  private byte[] salt;

  public byte[] getEncryptedSecret() {
    return this.encryptedSecret;
  }

  public byte[] getSalt() {
    return this.salt;
  }

  public void setEncryptedSecret(byte[] encryptedSecret) {
    this.encryptedSecret = encryptedSecret;
  }

  public void setSalt(byte[] salt) {
    this.salt = salt;
  }
}
