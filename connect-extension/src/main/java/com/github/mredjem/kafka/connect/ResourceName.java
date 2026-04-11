package com.github.mredjem.kafka.connect;

public class ResourceName {

  private final String organization;

  private final String environment;

  private final String cluster;

  private ResourceName(String organization, String environment, String cluster) {
    this.organization = organization;
    this.environment = environment;
    this.cluster = cluster;
  }

  public static ResourceName of(String organization, String environment, String cluster) {
    return new ResourceName(organization, environment, cluster);
  }

  public String getOrganizationUrn() {
    return String.format("crn://confluent.cloud/organization=%s", this.organization);
  }

  public String getEnvironmentUrn() {
    return String.format("crn://confluent.cloud/organization=%s/environment=%s", this.organization, this.environment);
  }

  public String getClusterUrn() {
    return String.format(
      "crn://confluent.cloud/organization=%s/environment=%s/cloud-cluster=%s",
      this.organization,
      this.environment,
      this.cluster
    );
  }

  @Override
  public String toString() {
    return this.getClusterUrn();
  }
}
