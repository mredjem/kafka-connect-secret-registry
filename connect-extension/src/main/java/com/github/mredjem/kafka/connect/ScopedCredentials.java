package com.github.mredjem.kafka.connect;

public class ScopedCredentials {

  public static final String READ_SCOPE = "read";

  private final String credentials;

  private final String scope;

  private ScopedCredentials(String credentials, String scope) {
    this.credentials = credentials;
    this.scope = scope;
  }

  public static ScopedCredentials of(String basicCredentials) {
    String[] credentialsParts = basicCredentials.split(":");

    if (credentialsParts.length == 3) {
      String originalCredentials = credentialsParts[0] + ":" + credentialsParts[1];

      return new ScopedCredentials(originalCredentials, credentialsParts[2]);
    }

    return new ScopedCredentials(basicCredentials, "");
  }

  public String getCredentials() {
    return this.credentials;
  }

  public String getScope() {
    return this.scope;
  }

  public boolean hasScope() {
    return !this.scope.isEmpty();
  }
}
