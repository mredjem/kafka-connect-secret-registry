package com.github.mredjem.kafka.connect;

import java.util.List;

public interface AuthorizationPort {

  boolean validateToken(AuthenticationCredentials authenticationCredentials);

  List<Role> getAppRoles(AuthenticationCredentials authenticationCredentials);
}
