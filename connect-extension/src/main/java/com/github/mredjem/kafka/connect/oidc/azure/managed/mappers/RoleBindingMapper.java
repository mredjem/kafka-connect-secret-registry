package com.github.mredjem.kafka.connect.oidc.azure.managed.mappers;

import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.RoleBinding;
import com.github.mredjem.kafka.connect.Scope;
import com.github.mredjem.kafka.connect.oidc.Roles;
import com.github.mredjem.kafka.connect.oidc.azure.managed.dtos.RoleBindingDto;

public final class RoleBindingMapper {

  private RoleBindingMapper() {}

  public static RoleBinding map(RoleBindingDto dto) {
    Role role = Roles.getRoles().get(dto.getRoleName());

    Scope scope = Scope.of(getScope(dto.getCrnPattern()));

    return RoleBinding.of(role, scope);
  }

  private static String getScope(String crnPattern) {
    String[] parts = crnPattern.split("/");

    String connectorPattern = parts[parts.length - 1];

    return connectorPattern.replaceFirst("^connector=", "");
  }
}
