package com.github.mredjem.kafka.connect.internals.utils;

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

  public static List<String> getClaimsAsList(String accessToken, String claimsName) throws JsonProcessingException {
    Object claims = parsePayload(accessToken).get(claimsName);

    if (!(claims instanceof List)) {
      return Collections.emptyList();
    }

    return ((List<?>) claims).stream()
      .map(Object::toString)
      .collect(Collectors.toList());
  }

  private static Map<String, Object> parsePayload(String accessToken) throws JsonProcessingException {
    String encodedPayload = accessToken.split("\\.")[1];

    String payload = new String(Base64.getDecoder().decode(encodedPayload));

    return OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});
  }
}
