package com.github.mredjem.kafka.connect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class RoleBinding {

  private final Role role;
  private final ResourceScope resourceScope;

  public boolean allows(Operation operation, String resourceName) {
    if (Operation.READ_CONFIGURATION == operation && "LIST_CONNECTOR_NAMES".equals(resourceName)) {
      return true;
    }

    if (Operation.READ_SECRET == operation && "LIST_SECRET_PATHS".equals(resourceName)) {
      return true;
    }

    return this.role.allows(operation) && this.resourceScope.matches(resourceName);
  }
}
