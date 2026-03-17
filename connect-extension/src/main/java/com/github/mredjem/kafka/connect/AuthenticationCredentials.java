package com.github.mredjem.kafka.connect;

public class AuthenticationCredentials {

  private final AuthenticationKind kind;

  private final String credentials;

  private AuthenticationCredentials(AuthenticationKind kind, String credentials) {
    this.kind = kind;
    this.credentials = credentials;
  }

  public static AuthenticationCredentials of(AuthenticationKind kind, String credentials) {
    return new AuthenticationCredentials(kind, credentials);
  }

  public AuthenticationKind getKind() {
    return this.kind;
  }

  public String getCredentials() {
    return this.credentials;
  }
}
