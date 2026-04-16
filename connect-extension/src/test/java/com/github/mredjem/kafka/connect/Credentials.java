package com.github.mredjem.kafka.connect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class Credentials {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private Credentials() {}

  public static String admin() {
    String credentials = "admin:password";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String centreon() {
    String credentials = "centreon:password";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String ci() {
    String credentials = "QRSTUVWXYZABCDEF:R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String confluentCloud() {
    String credentials = "ABCDEFGHIJKLMNOP:R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String servicePrincipal() {
    try {
      Map<String, String> claims = new HashMap<>();

      claims.put("iss", "https://login.microsoftonline.com/9bb441c4-edef-46ac-8a41-c49e44a3fd9a/v2.0");
      claims.put("aud", "confluent");
      claims.put("azp", "service_principal");

      String payload = OBJECT_MAPPER.writeValueAsString(claims);

      return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + Base64.getEncoder().encodeToString(payload.getBytes()) + ".y51fvVEpA109Nafbkild4Erhoxb_P-2HGO4SW7KkbDM";

    } catch (final JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
