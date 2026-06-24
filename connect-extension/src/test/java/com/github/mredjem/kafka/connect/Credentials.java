package com.github.mredjem.kafka.connect;

import com.google.gson.Gson;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class Credentials {

  private static final Gson GSON = new Gson();

  private Credentials() {}

  public static String ci() {
    String credentials = "QRSTUVWXYZABCDEF:R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String confluentCloud() {
    String credentials = "ABCDEFGHIJKLMNOP:R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String notFound() {
    String credentials = "APIKEYISNOTFOUND:R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987";

    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static String servicePrincipal() {
    Map<String, Object> claims = new HashMap<>();

    claims.put("iss", "https://login.microsoftonline.com/9bb441c4-edef-46ac-8a41-c49e44a3fd9a/v2.0");
    claims.put("aud", "confluent");
    claims.put("azp", "service_principal");

    String payload = GSON.toJson(claims);

    return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + Base64.getEncoder().encodeToString(payload.getBytes()) + ".y51fvVEpA109Nafbkild4Erhoxb_P-2HGO4SW7KkbDM";
  }

  public static String organizationAdmin() {
    Map<String, Object> claims = new HashMap<>();

    claims.put("iss", "https://login.microsoftonline.com/9bb441c4-edef-46ac-8a41-c49e44a3fd9a/v2.0");
    claims.put("aud", "confluent");
    claims.put("azp", "admin");

    String payload = GSON.toJson(claims);

    return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + Base64.getEncoder().encodeToString(payload.getBytes()) + ".y51fvVEpA109Nafbkild4Erhoxb_P-2HGO4SW7KkbDM";
  }
}
