package com.github.mredjem.kafka.connect.oidc.azure.ccloud;

import com.github.mredjem.kafka.connect.ResourceName;

import java.net.URI;

public class ConfluentResourceName implements ResourceName {

  private static final String CRN_SCHEME = "crn";

  private static final String CRN_AUTHORITY = "confluent.cloud";

  private final String organization;

  private final String environment;

  private final String cluster;

  private ConfluentResourceName(String crnPattern) {
    URI uri = URI.create(crnPattern);

    if (!CRN_SCHEME.equals(uri.getScheme()) || !CRN_AUTHORITY.equals(uri.getAuthority())) {
      throw new IllegalArgumentException("Invalid resource name: " + crnPattern);
    }

    String path = uri.getPath().replaceAll("^/", "");

    String[] resourceLevels = path.split("/");

    if (resourceLevels.length != 3) {
      throw new IllegalArgumentException("Invalid resource name: " + crnPattern);
    }

    this.organization = getResourceNameValue(resourceLevels[0], "organization");
    this.environment = getResourceNameValue(resourceLevels[1], "environment");
    this.cluster = getResourceNameValue(resourceLevels[2], "cloud-cluster");
  }

  public static ConfluentResourceName of(String crnPattern) {
    return new ConfluentResourceName(crnPattern);
  }

  @Override
  public String organizationUrn() {
    return String.format("crn://confluent.cloud/organization=%s", this.organization);
  }

  @Override
  public String environmentUrn() {
    return String.format("crn://confluent.cloud/organization=%s/environment=%s", this.organization, this.environment);
  }

  @Override
  public String clusterUrn() {
    return String.format(
      "crn://confluent.cloud/organization=%s/environment=%s/cloud-cluster=%s",
      this.organization,
      this.environment,
      this.cluster
    );
  }

  @Override
  public String toString() {
    return this.clusterUrn();
  }

  private String getResourceNameValue(String resourceName, String expectedResourceNameType) {
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
