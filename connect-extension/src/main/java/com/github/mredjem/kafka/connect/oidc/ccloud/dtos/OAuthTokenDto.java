package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthTokenDto {

  private String tokenType;
  private String accessToken;

  public String getAuthorization() {
    return this.tokenType + " " + this.accessToken;
  }
}
