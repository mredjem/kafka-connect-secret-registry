package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Key {

  private final Path path;

  private final String value;

  private Key(Path path, String value) {
    this.path = path;
    this.value = value;
  }

  public static Key of(String path, String value) {
    return new Key(Path.of(path), value);
  }

  public Path getPath() {
    return this.path;
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Key)) {
      return false;
    }

    return Objects.equals(this.path, ((Key) obj).path) && this.value.equals(((Key) obj).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.value);
  }

  @Override
  public String toString() {
    return "Key{" +
      "path=" + this.path +
      ", value='" + this.value + '\'' +
      '}';
  }
}
