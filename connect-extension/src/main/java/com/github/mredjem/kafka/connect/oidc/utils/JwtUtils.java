package com.github.mredjem.kafka.connect.oidc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ForbiddenException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JwtUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JwtUtils() {}

  public static Map<String, Object> checkIssuer(Map<String, Object> parsedPayload, String expectedIssuer) {
    String issuer = parsedPayload.getOrDefault("iss", "").toString();

    if (!issuer.contains(expectedIssuer)) {
      throw new ForbiddenException("Token issuer is not valid");
    }

    return parsedPayload;
  }

  public static boolean doesIdentityMatch(Map<String, Object> parsedPayload, String identityClaimName, String celFilter) {
    String identityClaimValue = JwtUtils.getClaim(parsedPayload, identityClaimName);

    String identityCelFilter = String.format("%s=='%s'", identityClaimName, identityClaimValue);

    return celFilter.replaceAll(" ", "").contains(identityCelFilter);
  }

  public static String getClaim(Map<String, Object> parsedPayload, String claimName) {
    String claim = claimName.startsWith("claims.") ? claimName.substring(7) : claimName;

    return parsedPayload.getOrDefault(claim, "").toString();
  }

  public static List<String> getClaimAsList(String accessToken, String claimName) {
    Object claim = parsePayload(accessToken).get(claimName);

    if (!(claim instanceof List)) {
      return Collections.emptyList();
    }

    return ((List<?>) claim).stream()
      .map(Object::toString)
      .collect(Collectors.toList());
  }

  public static Map<String, Object> parsePayload(String accessToken) {
    try {
      String encodedPayload = accessToken.split("\\.")[1];

      String payload = new String(Base64.getDecoder().decode(encodedPayload));

      return OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});

    } catch (final JsonProcessingException ignored) {
      return Collections.emptyMap();
    }
  }

  public static String encode(Map<String, String> configs, String... keys) {
    StringBuilder sb = new StringBuilder();

    for (String key : keys) {
      sb.append(configs.get(key)).append(":");
    }

    sb.deleteCharAt(sb.lastIndexOf(":"));

    return Base64.getEncoder().encodeToString(sb.toString().getBytes());
  }
}
