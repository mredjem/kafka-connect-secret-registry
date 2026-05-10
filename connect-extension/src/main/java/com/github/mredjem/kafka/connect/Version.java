package com.github.mredjem.kafka.connect;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Version {

  private static final int INIT_VERSION = 1;

  private final Key key;
  private final int value;

  public static Version of(String path, String key, int value) {
    return new Version(Key.of(Path.of(path), key), value);
  }

  public static Version init(String path, String key) {
    return Version.of(path, key, INIT_VERSION);
  }

  public Version nextVersion() {
    return Version.of(this.key.getPath().getValue(), this.key.getValue(), this.value + 1);
  }
}
