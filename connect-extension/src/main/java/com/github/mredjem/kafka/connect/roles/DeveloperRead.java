package com.github.mredjem.kafka.connect.roles;

import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.Role;

import java.util.EnumSet;
import java.util.Set;

import static com.github.mredjem.kafka.connect.Operation.READ_CONFIGURATION;
import static com.github.mredjem.kafka.connect.Operation.READ_STATUS;

public class DeveloperRead implements Role {

  @Override
  public Set<Operation> allowedOperations() {
    return EnumSet.of(READ_CONFIGURATION, READ_STATUS);
  }
}
