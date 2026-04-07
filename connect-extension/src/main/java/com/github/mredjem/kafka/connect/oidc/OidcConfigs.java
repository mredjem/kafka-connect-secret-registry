package com.github.mredjem.kafka.connect.oidc;

public final class OidcConfigs {

  public static final String OIDC_PREFIX = "confluent.cloud.";

  public static final String ORGANIZATION_ID_CONFIG = "organization.id";
  public static final String API_BASE_URL_CONFIG = "api.base.url";
  public static final String API_KEY_CONFIG = "api.key";
  public static final String API_SECRET_CONFIG = "api.secret";
  public static final String IDENTITY_PROVIDER_NAME_CONFIG = "identity.provider.name";

  private OidcConfigs() {}
}
