package com.github.mredjem.kafka.connect.providers;

public final class InternalSecretConfigs {

  public static final String PROVIDER_NAME = "secret";
  public static final String PROVIDER_PREFIX = "config.providers.%s.param.";

  public static final String KAFKASTORE_TOPIC_CONFIG = "kafkastore.topic";
  public static final String KAFKASTORE_TOPIC_REPLICATION_FACTOR_CONFIG = "kafkastore.topic.replication.factor";
  public static final String MASTER_ENCRYPTION_KEY_CONFIG = "master.encryption.key";
  public static final String SECRET_REGISTRY_GROUP_ID_CONFIG = "secret.registry.group.id";

  private InternalSecretConfigs() {
  }
}
