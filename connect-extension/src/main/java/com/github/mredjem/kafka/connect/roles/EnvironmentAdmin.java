package com.github.mredjem.kafka.connect.roles;

import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.Role;

import java.util.EnumSet;
import java.util.Set;

public class EnvironmentAdmin implements Role {

  @Override
  public Set<Operation> allowedOperations() {
    return EnumSet.allOf(Operation.class);
  }
}
