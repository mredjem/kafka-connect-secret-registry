package com.github.mredjem.kafka.connect;

import lombok.Getter;

import java.util.Base64;

@Getter
public class AuthenticationCredentials {

  private final String authorization;
  private final AuthenticationKind kind;
  private final String credentials;

  private AuthenticationCredentials(String authorization) {
    this.authorization = authorization;
    this.kind = AuthenticationKind.fromAuthorization(authorization);
    this.credentials = AuthenticationKind.NONE != this.kind ? authorization.substring(this.kind.toString().length() + 1) : "";
  }

  public static AuthenticationCredentials of(String authorization) {
    return new AuthenticationCredentials(authorization);
  }

  public static AuthenticationCredentials of(String username, String password) {
    String basicCredentials = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

    return new AuthenticationCredentials(basicCredentials);
  }
}
