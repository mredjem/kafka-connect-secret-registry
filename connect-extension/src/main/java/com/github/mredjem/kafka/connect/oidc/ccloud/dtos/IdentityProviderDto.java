package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityProviderDto {

  private String id;
  private String displayName;
  private String state;
  private String jwksStatus;
}
