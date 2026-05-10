package com.github.mredjem.kafka.connect.oidc;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OidcConfigs {

  public final String OIDC_PREFIX = "confluent.cloud.";

  public final String CLUSTER_CRN_PATTERN_CONFIG = "cluster.crn.pattern";
  public final String API_BASE_URL_CONFIG = "api.base.url";
  public final String API_KEY_CONFIG = "api.key";
  public final String API_SECRET_CONFIG = "api.secret";
  public final String IDENTITY_PROVIDER_NAME_CONFIG = "identity.provider.name";
}
