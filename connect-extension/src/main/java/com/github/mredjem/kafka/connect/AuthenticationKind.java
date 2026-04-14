package com.github.mredjem.kafka.connect;

public enum AuthenticationKind {
  BASIC,
  BEARER,
  NONE;

  public static AuthenticationKind fromAuthorization(String authorization) {
    if (authorization == null || authorization.isEmpty()) {
      return NONE;
    }

    if (authorization.toLowerCase().startsWith("basic ")) {
      return BASIC;
    }

    if (authorization.toLowerCase().startsWith("bearer ")) {
      return BEARER;
    }

    return NONE;
  }
}
