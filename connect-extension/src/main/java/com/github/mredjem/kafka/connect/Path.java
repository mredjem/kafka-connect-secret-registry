package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Path {

  private final String value;

  private Path(String value) {
    this.value = value;
  }

  public static Path of(String value) {
    return new Path(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Path)) {
      return false;
    }

    return this.value.equals(((Path) obj).value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.value);
  }

  @Override
  public String toString() {
    return "Path{" +
      "value='" + this.value + '\'' +
      '}';
  }
}
