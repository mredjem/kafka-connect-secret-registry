package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityPoolDto {

  private String id;
  private String filter;
  private String state;
}
