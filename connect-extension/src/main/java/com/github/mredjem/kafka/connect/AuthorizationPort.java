package com.github.mredjem.kafka.connect;

public interface AuthorizationPort {

  boolean checkAccess(AuthenticationCredentials authenticationCredentials, Operation operation, String resourceName);
}
