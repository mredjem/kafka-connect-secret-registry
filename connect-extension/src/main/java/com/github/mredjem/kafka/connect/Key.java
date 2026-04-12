package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Key {

  private final Path path;

  private final String key;

  private Key(Path path, String key) {
    this.path = path;
    this.key = key;
  }

  public static Key of(String path, String key) {
    return new Key(Path.of(path), key);
  }

  public Path getPath() {
    return this.path;
  }

  public String getKey() {
    return this.key;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Key)) {
      return false;
    }

    return Objects.equals(this.path, ((Key) obj).path) && this.key.equals(((Key) obj).key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.key);
  }

  @Override
  public String toString() {
    return "Key{" +
      "path=" + this.path +
      ", key='" + this.key + '\'' +
      '}';
  }
}
