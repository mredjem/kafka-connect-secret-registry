package com.github.mredjem.kafka.connect.oidc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JwtUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JwtUtils() {}

  public static List<String> getClaimAsList(String accessToken, String claimName) {
    Object claim = parseClaims(accessToken).get(claimName);

    if (!(claim instanceof List)) {
      return Collections.emptyList();
    }

    return ((List<?>) claim).stream()
      .map(Object::toString)
      .collect(Collectors.toList());
  }

  public static Map<String, Object> parseClaims(String accessToken) {
    try {
      String encodedPayload = accessToken.split("\\.")[1];

      String payload = new String(Base64.getDecoder().decode(encodedPayload));

      return OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});

    } catch (final JsonProcessingException ignored) {
      return Collections.emptyMap();
    }
  }
}
