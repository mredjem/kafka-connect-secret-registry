package com.github.mredjem.kafka.connect.internals;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class KafkaAuthorizationRepository implements AuthorizationPort {

  private final OidcPort oidcPort;

  @Override
  public boolean checkAccess(AuthenticationCredentials authenticationCredentials, Operation operation, String resourceName) {
    boolean validCredentials = this.oidcPort.validateCredentials(authenticationCredentials);

    if (!validCredentials) {
      return false;
    }

    return this.oidcPort.getRoleBindings(authenticationCredentials)
      .stream()
      .anyMatch(roleBinding -> roleBinding.allows(operation, resourceName));
  }
}
