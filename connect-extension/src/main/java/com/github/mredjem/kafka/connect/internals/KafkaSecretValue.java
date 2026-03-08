package com.github.mredjem.kafka.connect.internals;

public class KafkaSecretValue {

  private String path;

  private String key;

  private int version;

  private KafkaSecretEncrypted encrypted;

  private String checksum;

  private String createdBy;

  private long createdAt;

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getVersion() {
    return this.version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public KafkaSecretEncrypted getEncrypted() {
    return this.encrypted;
  }

  public void setEncrypted(KafkaSecretEncrypted encrypted) {
    this.encrypted = encrypted;
  }

  public String getChecksum() {
    return this.checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public long getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }
}
