package com.github.mredjem.kafka.connect.oidc.roles;

import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.Role;

import java.util.EnumSet;
import java.util.Set;

import static com.github.mredjem.kafka.connect.Operation.CONFIGURE;
import static com.github.mredjem.kafka.connect.Operation.DELETE;
import static com.github.mredjem.kafka.connect.Operation.PAUSE_RESUME_RESTART;
import static com.github.mredjem.kafka.connect.Operation.READ_CONFIGURATION;
import static com.github.mredjem.kafka.connect.Operation.READ_STATUS;

public class ResourceOwner implements Role {

  @Override
  public Set<Operation> allowedOperations() {
    return EnumSet.of(CONFIGURE, DELETE, PAUSE_RESUME_RESTART, READ_CONFIGURATION, READ_STATUS);
  }
}
