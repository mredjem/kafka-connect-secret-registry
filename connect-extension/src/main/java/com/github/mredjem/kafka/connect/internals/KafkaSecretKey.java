package com.github.mredjem.kafka.connect.internals;

import java.util.Objects;

public class KafkaSecretKey {

  private String keyType = "SECRET";

  private String path;

  private int version;

  private String key;

  public String getKeyType() {
    return this.keyType;
  }

  public void setKeyType(String keyType) {
    this.keyType = keyType;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getVersion() {
    return this.version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof KafkaSecretKey)) {
      return false;
    }

    return this.version == ((KafkaSecretKey) obj).version && Objects.equals(this.path, ((KafkaSecretKey) obj).path) && Objects.equals(this.key, ((KafkaSecretKey) obj).key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.version, this.key);
  }

  @Override
  public String toString() {
    return "KafkaSecretKey{" +
      "path='" + path + '\'' +
      ", version=" + version +
      ", key='" + key + '\'' +
      '}';
  }
}
