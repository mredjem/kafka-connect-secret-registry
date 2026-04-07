package com.github.mredjem.kafka.connect.oidc.azure.managed.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityPoolDto {

  private String id;

  @JsonProperty("identity_claim")
  private String identityClaim;

  private String filter;

  private String state;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIdentityClaim() {
    return this.identityClaim;
  }

  public void setIdentityClaim(String identityClaim) {
    this.identityClaim = identityClaim;
  }

  public String getFilter() {
    return this.filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public String getState() {
    return this.state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
