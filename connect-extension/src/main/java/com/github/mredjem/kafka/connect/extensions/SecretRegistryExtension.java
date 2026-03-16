package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.internals.KafkaInternalTopicRepository;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

import java.util.Map;

import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.PROVIDER_NAME;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.PROVIDER_PREFIX;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG;

public class SecretRegistryExtension implements ConnectRestExtension {

  private SecretRegistryApi secretRegistryApi;

  @Override
  public String version() {
    return "1";
  }

  @Override
  public void configure(Map<String, ?> configs) {
    Map<String, String> providerConfigs = ConfigUtils.getConfigsForPrefix(String.format(PROVIDER_PREFIX, PROVIDER_NAME), configs);

    String registryGroupId = providerConfigs.get(SECRET_REGISTRY_GROUP_ID_CONFIG);

    SecretRegistryPort secretRegistryPort = KafkaInternalTopicRepository.create(ConfigUtils.addEntry(
      providerConfigs,
      SECRET_REGISTRY_GROUP_ID_CONFIG,
      registryGroupId + "-rest"
    ));

    this.secretRegistryApi = SecretRegistryApi.create(secretRegistryPort);
  }

  @Override
  public void register(ConnectRestExtensionContext restPluginContext) {
    restPluginContext.configurable()
      .register(SecretRegistryExceptionHandler.class)
      .register(secretRegistryApi);
  }

  @Override
  public void close() {
    // noop
  }
}
