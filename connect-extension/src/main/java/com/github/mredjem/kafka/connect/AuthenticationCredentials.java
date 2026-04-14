package com.github.mredjem.kafka.connect;

public class AuthenticationCredentials {

  private final AuthenticationKind kind;

  private final String credentials;

  private AuthenticationCredentials(String authorization) {
    this.kind = AuthenticationKind.fromAuthorization(authorization);
    this.credentials = AuthenticationKind.NONE != this.kind ? authorization.substring(this.kind.toString().length() + 1) : "";
  }

  public static AuthenticationCredentials of(String authorization) {
    return new AuthenticationCredentials(authorization);
  }

  public AuthenticationKind getKind() {
    return this.kind;
  }

  public String getCredentials() {
    return this.credentials;
  }
}
