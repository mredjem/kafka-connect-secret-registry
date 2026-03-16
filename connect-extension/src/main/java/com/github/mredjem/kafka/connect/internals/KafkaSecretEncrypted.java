package com.github.mredjem.kafka.connect.internals;

public class KafkaSecretEncrypted {

  private String derivationInfo;

  private byte[] content;

  private byte[] salt;

  public String getDerivationInfo() {
    return this.derivationInfo;
  }

  public void setDerivationInfo(String derivationInfo) {
    this.derivationInfo = derivationInfo;
  }

  public byte[] getContent() {
    return this.content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public byte[] getSalt() {
    return this.salt;
  }

  public void setSalt(byte[] salt) {
    this.salt = salt;
  }
}
