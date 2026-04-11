package com.github.mredjem.kafka.connect.oidc.azure.ccloud.mappers;

import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.RoleBinding;
import com.github.mredjem.kafka.connect.ResourceScope;
import com.github.mredjem.kafka.connect.Scope;
import com.github.mredjem.kafka.connect.oidc.Roles;
import com.github.mredjem.kafka.connect.oidc.azure.ccloud.dtos.RoleBindingDto;

import java.util.Set;

public final class RoleBindingMapper {

  private RoleBindingMapper() {}

  public static RoleBinding map(RoleBindingDto dto) {
    Role role = Roles.getRoles().get(dto.getRoleName());

    if (role == null) {
      return null;
    }

    Set<Scope> roleScopes = role.applicableScopes();

    if (roleScopes.contains(Scope.CONNECTOR)) {
      String resource = getResource(dto.getCrnPattern());

      return RoleBinding.of(role, ResourceScope.of(Scope.CONNECTOR, resource));
    }

    Scope scope = roleScopes.iterator().next();

    return RoleBinding.of(role, ResourceScope.of(scope, "*"));
  }

  private static String getResource(String crnPattern) {
    String[] parts = crnPattern.split("/", -1);

    String connectorPattern = parts[parts.length - 1];

    return connectorPattern.replaceFirst("^connector=", "");
  }
}
