package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Version {

  private static final int INIT_VERSION = 1;

  private final Key key;

  private final int value;

  private Version(Key key, int value) {
    this.key = key;
    this.value = value;
  }

  public static Version of(String path, String key, int value) {
    return new Version(Key.of(path, key), value);
  }

  public static Version init(String path, String key) {
    return Version.of(path, key, INIT_VERSION);
  }

  public Key getKey() {
    return this.key;
  }

  public int getValue() {
    return this.value;
  }

  public Version nextVersion() {
    return new Version(this.key, this.value + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Version)) {
      return false;
    }

    return this.value == ((Version) obj).value && Objects.equals(this.key, ((Version) obj).key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key, this.value);
  }

  @Override
  public String toString() {
    return "Version{" +
      "key=" + this.key +
      ", value=" + this.value +
      '}';
  }
}
