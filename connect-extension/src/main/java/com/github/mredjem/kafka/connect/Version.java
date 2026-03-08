package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Version {

  private static final int INIT_VERSION = 1;

  private final Key key;
  private final int version;

  private Version(Key key, int version) {
    this.key = key;
    this.version = version;
  }

  public static Version of(String path, String key, int version) {
    return new Version(Key.of(path, key), version);
  }

  public static Version init(String path, String key) {
    return Version.of(path, key, INIT_VERSION);
  }

  public Key getKey() {
    return this.key;
  }

  public int getVersion() {
    return this.version;
  }

  public Version nextVersion() {
    return new Version(this.key, this.version + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Version)) {
      return false;
    }

    return this.version == ((Version) obj).version && Objects.equals(this.key, ((Version) obj).key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key, this.version);
  }

  @Override
  public String toString() {
    return "Version{" +
      "key=" + this.key +
      ", version=" + this.version +
      '}';
  }
}
