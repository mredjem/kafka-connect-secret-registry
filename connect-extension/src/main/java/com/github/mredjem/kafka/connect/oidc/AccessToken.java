package com.github.mredjem.kafka.connect.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Getter
public class AccessToken {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, Object> claims;

  private AccessToken(String accessToken) {
    try {
      String encodedPayload = accessToken.split("\\.")[1];

      String payload = new String(Base64.getDecoder().decode(encodedPayload));

      Map<String, Object> tokenClaims = OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});

      this.claims = Collections.unmodifiableMap(tokenClaims);

    } catch (final JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to parse token", e);
    }
  }

  public static AccessToken parse(String accessToken) {
    return new AccessToken(accessToken);
  }
}
