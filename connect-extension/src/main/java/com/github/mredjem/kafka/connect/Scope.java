package com.github.mredjem.kafka.connect;

import java.util.Arrays;

public enum Scope {

  CONNECTOR,
  CLUSTER,
  ENVIRONMENT,
  ORGANIZATION;

  public static Scope fromName(String name) {
    return Arrays.stream(values())
      .filter(scope -> scope.name().equalsIgnoreCase(name))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Unknown scope '" + name + "'"));
  }
}
