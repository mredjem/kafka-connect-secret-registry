package com.github.mredjem.kafka.connect.oidc.roles;

import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.Scope;

import java.util.EnumSet;
import java.util.Set;

public class OrganizationAdmin implements Role {

  @Override
  public Set<Scope> applicableScopes() {
    return EnumSet.of(Scope.ORGANIZATION);
  }

  @Override
  public Set<Operation> allowedOperations() {
    return EnumSet.allOf(Operation.class);
  }
}
