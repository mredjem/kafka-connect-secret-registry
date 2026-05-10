package com.github.mredjem.kafka.connect.oidc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OidcConfigs {

  public static final String OIDC_PREFIX = "confluent.cloud.";

  public static final String CLUSTER_CRN_PATTERN_CONFIG = "cluster.crn.pattern";
  public static final String API_BASE_URL_CONFIG = "api.base.url";
  public static final String API_KEY_CONFIG = "api.key";
  public static final String API_SECRET_CONFIG = "api.secret";
  public static final String IDENTITY_PROVIDER_NAME_CONFIG = "identity.provider.name";
}
