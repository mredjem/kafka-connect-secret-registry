package com.github.mredjem.kafka.connect.oidc.azure.managed;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.RoleBinding;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.azure.managed.mappers.RoleBindingMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfluentCloudRepository implements OidcPort {

  private final ConfluentCloudClient client;

  private ConfluentCloudRepository(Map<String, String> configs) {
    this.client = ConfluentCloudClient.create(configs);
  }

  public static ConfluentCloudRepository create(Map<String, String> configs) {
    return new ConfluentCloudRepository(configs);
  }

  @Override
  public boolean validateCredentials(AuthenticationCredentials authenticationCredentials) {
    return AuthenticationKind.BEARER == authenticationCredentials.getKind();
  }

  @Override
  public List<RoleBinding> getRoleBindings(AuthenticationCredentials authenticationCredentials) {
    return this.client.listRoleBindings(authenticationCredentials)
      .stream()
      .map(RoleBindingMapper::map)
      .collect(Collectors.toList());
  }
}
