package com.github.mredjem.kafka.connect;

public class Scope {

  public static final Scope ALL = new Scope("*");

  private final String scope;

  private Scope(String scope) {
    if (scope == null || scope.isEmpty()) {
      throw new IllegalArgumentException("scope cannot be null or empty");
    }

    this.scope = scope;
  }

  public static Scope of(String scope) {
    return new Scope(scope);
  }

  public boolean matches(String resourceName) {
    if (resourceName.isEmpty() || "*".equals(this.scope)) {
      return true;
    }

    if (this.scope.endsWith("*")) {
      return resourceName.startsWith(this.scope.substring(0, this.scope.length() - 1));
    }

    return this.scope.equals(resourceName);
  }

  @Override
  public String toString() {
    return "Scope{" +
      "scope='" + this.scope + '\'' +
      '}';
  }
}
