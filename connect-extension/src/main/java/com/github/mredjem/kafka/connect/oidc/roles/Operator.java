package com.github.mredjem.kafka.connect.oidc.roles;

import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.Scope;

import java.util.EnumSet;
import java.util.Set;

public class Operator implements Role {

  @Override
  public Set<Scope> applicableScopes() {
    return EnumSet.of(Scope.CLUSTER, Scope.ENVIRONMENT, Scope.ORGANIZATION);
  }

  @Override
  public Set<Operation> allowedOperations() {
    return EnumSet.of(Operation.PAUSE_RESUME_RESTART, Operation.READ_STATUS);
  }
}
