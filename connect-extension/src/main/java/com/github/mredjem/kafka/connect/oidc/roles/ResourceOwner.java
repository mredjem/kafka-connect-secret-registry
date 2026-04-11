package com.github.mredjem.kafka.connect.oidc.roles;

import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.Scope;

import java.util.EnumSet;
import java.util.Set;

public class ResourceOwner implements Role {

  @Override
  public Set<Scope> applicableScopes() {
    return EnumSet.of(Scope.CONNECTOR);
  }

  @Override
  public Set<Operation> allowedOperations() {
    return EnumSet.of(Operation.CONFIGURE, Operation.DELETE, Operation.PAUSE_RESUME_RESTART, Operation.READ_CONFIGURATION, Operation.READ_STATUS);
  }
}
