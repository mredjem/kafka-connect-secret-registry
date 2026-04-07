package com.github.mredjem.kafka.connect.oidc;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.RoleBinding;

import java.util.List;

public interface OidcPort {

  boolean validateCredentials(AuthenticationCredentials authenticationCredentials);

  List<RoleBinding> getRoleBindings(AuthenticationCredentials authenticationCredentials);
}
