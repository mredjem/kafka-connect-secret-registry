package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleBindingDto {

  @JsonProperty("role_name")
  private String roleName;

  @JsonProperty("crn_pattern")
  private String crnPattern;

  public String getRoleName() {
    return this.roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getCrnPattern() {
    return this.crnPattern;
  }

  public void setCrnPattern(String crnPattern) {
    this.crnPattern = crnPattern;
  }
}
