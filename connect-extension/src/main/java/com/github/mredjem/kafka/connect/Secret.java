package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Secret {

  private final Version version;
  private final String secret;

  private Secret(Version version, String secret) {
    this.version = version;
    this.secret = secret;
  }

  public static Secret of(String path, String key, int version, String secret) {
    return new Secret(Version.of(path, key, version), secret);
  }

  public Version getVersion() {
    return this.version;
  }

  public String getSecret() {
    return this.secret;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Secret)) {
      return false;
    }

    return Objects.equals(this.version, ((Secret) obj).version) && Objects.equals(this.secret, ((Secret) obj).secret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.secret);
  }

  @Override
  public String toString() {
    return "Secret{" +
      "version=" + version +
      '}';
  }
}
