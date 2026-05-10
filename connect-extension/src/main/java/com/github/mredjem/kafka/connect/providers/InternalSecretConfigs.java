package com.github.mredjem.kafka.connect.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalSecretConfigs {

  public static final String PROVIDER_PREFIX = "config.providers.secret.param.";
  public static final String KAFKASTORE_PREFIX = "kafkastore.";
  public static final String KAFKASTORE_TOPIC_CONFIG = KAFKASTORE_PREFIX + "topic";
  public static final String KAFKASTORE_TOPIC_REPLICATION_FACTOR_CONFIG = KAFKASTORE_PREFIX + "topic.replication.factor";
  public static final String MASTER_ENCRYPTION_KEY_CONFIG = "master.encryption.key";
  public static final String SECRET_REGISTRY_GROUP_ID_CONFIG = "secret.registry.group.id";
}
