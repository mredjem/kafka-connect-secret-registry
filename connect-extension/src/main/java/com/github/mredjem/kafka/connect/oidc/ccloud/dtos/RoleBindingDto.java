package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleBindingDto {

  private String roleName;
  private String crnPattern;
}
