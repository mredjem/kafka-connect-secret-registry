package com.github.mredjem.kafka.connect.oidc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class AccessToken {

  private static final Gson GSON = new Gson();

  private final Map<String, Object> claims;

  private AccessToken(String accessToken) {
    try {
      String encodedPayload = accessToken.split("\\.")[1];

      String payload = new String(Base64.getDecoder().decode(encodedPayload));

      Map<String, Object> tokenClaims = GSON.fromJson(payload, new TypeToken<HashMap<String, Object>>() {}.getType());

      this.claims = Collections.unmodifiableMap(tokenClaims);

    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to parse token", e);
    }
  }

  public static AccessToken parse(String accessToken) {
    return new AccessToken(accessToken);
  }
}
