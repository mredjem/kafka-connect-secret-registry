package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityProviderDto {

  private String id;
  @JsonProperty("display_name")
  private String displayName;
  private String state;
  @JsonProperty("jwks_status")
  private String jwksStatus;
}
