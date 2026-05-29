package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthTokenDto {

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("access_token")
  private String accessToken;

  public String getAuthorization() {
    return this.tokenType + " " + this.accessToken;
  }
}
