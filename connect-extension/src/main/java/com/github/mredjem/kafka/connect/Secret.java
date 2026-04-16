package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Secret {

  private final Version version;

  private final String value;

  private Secret(Version version, String value) {
    this.version = version;
    this.value = value;
  }

  public static Secret of(String path, String key, int version, String value) {
    return new Secret(Version.of(path, key, version), value);
  }

  public Version getVersion() {
    return this.version;
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Secret)) {
      return false;
    }

    return Objects.equals(this.version, ((Secret) obj).version) && Objects.equals(this.value, ((Secret) obj).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.value);
  }

  @Override
  public String toString() {
    return "Secret{" +
      "version=" + version +
      '}';
  }
}
