package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleBindingDto {

  @JsonProperty("role_name")
  private String roleName;
  @JsonProperty("crn_pattern")
  private String crnPattern;
}
