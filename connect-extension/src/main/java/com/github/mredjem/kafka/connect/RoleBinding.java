package com.github.mredjem.kafka.connect;

public class RoleBinding {

  private final Role role;

  private final ResourceScope resourceScope;

  private RoleBinding(Role role, ResourceScope resourceScope) {
    this.role = role;
    this.resourceScope = resourceScope;
  }

  public static RoleBinding of(Role role, ResourceScope resourceScope) {
    return new RoleBinding(role, resourceScope);
  }

  public Role getRole() {
    return this.role;
  }

  public ResourceScope getResourceScope() {
    return this.resourceScope;
  }

  public boolean allows(Operation operation, String resourceName) {
    if (Operation.READ_CONFIGURATION == operation && "LIST_CONNECTOR_NAMES".equals(resourceName)) {
      return true;
    }

    if (Operation.READ_SECRET == operation && "LIST_SECRET_PATHS".equals(resourceName)) {
      return true;
    }

    return this.role.allows(operation) && this.resourceScope.matches(resourceName);
  }

  @Override
  public String toString() {
    return "RoleBinding{" +
      "role=" + this.role +
      ", resourceScope=" + this.resourceScope +
      '}';
  }
}
