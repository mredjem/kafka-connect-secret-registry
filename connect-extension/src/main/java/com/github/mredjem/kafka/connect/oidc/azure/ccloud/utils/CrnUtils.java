package com.github.mredjem.kafka.connect.oidc.azure.ccloud.utils;

import com.github.mredjem.kafka.connect.ResourceName;

import java.net.URI;

public final class CrnUtils {

  private static final String CRN_SCHEMA = "crn";

  private static final String CRN_AUTHORITY = "confluent.cloud";

  private CrnUtils() {}

  public static ResourceName parseCrnPattern(String crnPattern) {
    URI uri = URI.create(crnPattern);

    if (!CRN_SCHEMA.equals(uri.getScheme()) || !CRN_AUTHORITY.equals(uri.getAuthority())) {
      throw new IllegalArgumentException("Invalid resource name: " + crnPattern);
    }

    String path = uri.getPath().replaceAll("^/", "");

    String[] resourceLevels = path.split("/");

    if (resourceLevels.length != 3) {
      throw new IllegalArgumentException("Invalid resource name: " + crnPattern);
    }

    return ResourceName.of(
      getResourceNameValue(resourceLevels[0], "organization"),
      getResourceNameValue(resourceLevels[1], "environment"),
      getResourceNameValue(resourceLevels[2], "cloud-cluster")
    );
  }

  private static String getResourceNameValue(String resourceName, String expectedResourceNameType) {
    String[] pairs = resourceName.split("=");

    if (pairs.length != 2) {
      throw new IllegalArgumentException("Invalid resource name: " + resourceName);
    }

    String resourceNameType = pairs[0];

    if (!expectedResourceNameType.equals(resourceNameType)) {
      throw new IllegalArgumentException("Invalid resource name: " + resourceName);
    }

    return pairs[1];
  }
}
