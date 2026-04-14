package com.github.mredjem.kafka.connect;

import java.util.Base64;

public class ScopedCredentials {

  private static final String READ_SCOPE = "read";

  private final String credentials;

  private final String scope;

  private ScopedCredentials(String credentials, String scope) {
    this.credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
    this.scope = scope;
  }

  public static ScopedCredentials of(String basicCredentials) {
    String[] credentialsParts = basicCredentials.split(":", -1);

    if (credentialsParts.length == 3) {
      String originalCredentials = credentialsParts[0] + ":" + credentialsParts[1];

      return new ScopedCredentials(originalCredentials, credentialsParts[2]);
    }

    return new ScopedCredentials(basicCredentials, "");
  }

  public String getCredentials() {
    return this.credentials;
  }

  public boolean hasReadScope() {
    return READ_SCOPE.equalsIgnoreCase(this.scope);
  }
}
