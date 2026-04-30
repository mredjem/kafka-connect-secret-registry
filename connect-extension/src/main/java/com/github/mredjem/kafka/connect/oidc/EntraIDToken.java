package com.github.mredjem.kafka.connect.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class EntraIDToken {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, Object> claims;

  private EntraIDToken(String accessToken) {
    try {
      String encodedPayload = accessToken.split("\\.")[1];

      String payload = new String(Base64.getDecoder().decode(encodedPayload));

      Map<String, Object> tokenClaims = OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});

      this.claims = Collections.unmodifiableMap(tokenClaims);

    } catch (final JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to parse token", e);
    }
  }

  public static EntraIDToken parse(String accessToken) {
    return new EntraIDToken(accessToken);
  }

  public Map<String, Object> getClaims() {
    return this.claims;
  }
}
