package com.github.mredjem.kafka.connect.oidc.azure.managed.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityProviderDto {

  private String id;

  @JsonProperty("display_name")
  private String displayName;

  private String state;

  @JsonProperty("jwks_status")
  private String jwksStatus;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getState() {
    return this.state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getJwksStatus() {
    return this.jwksStatus;
  }

  public void setJwksStatus(String jwksStatus) {
    this.jwksStatus = jwksStatus;
  }
}
