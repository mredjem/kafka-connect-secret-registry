package com.github.mredjem.kafka.connect.oidc;

import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.oidc.roles.CloudClusterAdmin;
import com.github.mredjem.kafka.connect.oidc.roles.ConnectManager;
import com.github.mredjem.kafka.connect.oidc.roles.DeveloperRead;
import com.github.mredjem.kafka.connect.oidc.roles.DeveloperWrite;
import com.github.mredjem.kafka.connect.oidc.roles.EnvironmentAdmin;
import com.github.mredjem.kafka.connect.oidc.roles.Operator;
import com.github.mredjem.kafka.connect.oidc.roles.OrganizationAdmin;
import com.github.mredjem.kafka.connect.oidc.roles.ResourceOwner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Roles {

  private static final Map<String, Role> ROLES_BY_NAME;

  static {
    List<Role> roles = new ArrayList<>();

    roles.add(new DeveloperRead());
    roles.add(new DeveloperWrite());
    roles.add(new Operator());
    roles.add(new ConnectManager());
    roles.add(new ResourceOwner());
    roles.add(new CloudClusterAdmin());
    roles.add(new EnvironmentAdmin());
    roles.add(new OrganizationAdmin());

    ROLES_BY_NAME = roles.stream().collect(Collectors.toMap(Role::roleName, Function.identity()));
  }

  private Roles() {}

  public static Map<String, Role> getRoles() {
    return Collections.unmodifiableMap(ROLES_BY_NAME);
  }
}
