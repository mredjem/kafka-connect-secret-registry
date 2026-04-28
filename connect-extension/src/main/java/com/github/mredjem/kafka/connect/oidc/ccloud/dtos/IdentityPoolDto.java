package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

public class IdentityPoolDto {

  private String id;

  private String filter;

  private String state;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
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
