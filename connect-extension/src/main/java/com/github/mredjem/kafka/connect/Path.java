package com.github.mredjem.kafka.connect;

import java.util.Objects;

public class Path {

  private final String path;

  private Path(String path) {
    this.path = path;
  }

  public static Path of(String path) {
    return new Path(path);
  }

  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Path)) {
      return false;
    }

    return this.path.equals(((Path) obj).path);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.path);
  }

  @Override
  public String toString() {
    return "Path{" +
      "path='" + this.path + '\'' +
      '}';
  }
}
