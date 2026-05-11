package com.github.mredjem.kafka.connect;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class Version {

  private final Key key;
  private final int value;

  public Secret secret(String secret) {
    return Secret.of(this, secret);
  }

  public Version nextVersion() {
    return Version.of(this.key, this.value + 1);
  }
}
