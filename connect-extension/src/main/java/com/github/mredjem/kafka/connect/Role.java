package com.github.mredjem.kafka.connect;

import java.util.Set;

public interface Role {

  Set<Operation> allowedOperations();

  Set<Scope> applicableScopes();

  default String roleName() {
    return this.getClass().getSimpleName();
  }

  default boolean allows(Operation operation) {
    return this.allowedOperations().contains(operation);
  }
}
