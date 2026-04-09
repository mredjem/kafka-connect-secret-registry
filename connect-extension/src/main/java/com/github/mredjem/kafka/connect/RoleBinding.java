package com.github.mredjem.kafka.connect;

public class RoleBinding {

  private final Role role;

  private final Scope scope;

  private RoleBinding(Role role, Scope scope) {
    this.role = role;
    this.scope = scope;
  }

  public static RoleBinding of(Role role, Scope scope) {
    return new RoleBinding(role, scope);
  }

  public Role getRole() {
    return this.role;
  }

  public Scope getScope() {
    return this.scope;
  }
}
