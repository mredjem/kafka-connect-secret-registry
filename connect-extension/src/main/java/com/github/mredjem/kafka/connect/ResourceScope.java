package com.github.mredjem.kafka.connect;

public class ResourceScope {

  public static final ResourceScope ALL = new ResourceScope(Scope.CONNECTOR, "*");

  private final Scope scope;

  private final String resource;

  private ResourceScope(Scope scope, String resource) {
    this.scope = scope;
    this.resource = resource;
  }

  public static ResourceScope of(Scope scope, String resource) {
    return new ResourceScope(scope, resource);
  }

  public boolean matches(String resourceName) {
    if (Scope.CONNECTOR != this.scope || "*".equals(this.resource)) {
      return true;
    }

    if (this.resource.endsWith("*")) {
      return resourceName.startsWith(this.resource.substring(0, this.resource.length() - 1));
    }

    return this.resource.equals(resourceName);
  }

  @Override
  public String toString() {
    return "ResourceScope{" +
      "scope=" + this.scope +
      ", resource='" + this.resource + '\'' +
      '}';
  }
}
