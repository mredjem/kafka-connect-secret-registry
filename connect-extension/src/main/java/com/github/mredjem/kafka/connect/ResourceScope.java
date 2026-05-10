package com.github.mredjem.kafka.connect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class ResourceScope {

  private final Scope scope;
  private final String resource;

  public boolean matches(String resourceName) {
    if (Scope.CONNECTOR != this.scope || "*".equals(this.resource)) {
      return true;
    }

    if (this.resource.endsWith("*")) {
      return resourceName.startsWith(this.resource.substring(0, this.resource.length() - 1));
    }

    return this.resource.equals(resourceName);
  }
}
