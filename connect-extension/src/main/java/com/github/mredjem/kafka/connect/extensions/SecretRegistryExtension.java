package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApi;
import com.github.mredjem.kafka.connect.internals.KafkaInternalTopicRepository;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigs;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

import java.io.IOException;
import java.util.Map;

public class SecretRegistryExtension implements ConnectRestExtension {

  private SecretRegistryPort secretRegistryPort;

  @Override
  public String version() {
    return "1";
  }

  @Override
  public void configure(Map<String, ?> configs) {
    this.secretRegistryPort = doConfigure(configs);
  }

  @Override
  public void register(ConnectRestExtensionContext restPluginContext) {
    restPluginContext.configurable().register(SecretRegistryApi.create(this.secretRegistryPort));
  }

  @Override
  public void close() throws IOException {
    if (this.secretRegistryPort != null) {
      this.secretRegistryPort.close();
    }
  }

  private SecretRegistryPort doConfigure(Map<String, ?> configs) {
    Map<String, String> extensionConfigs = ConfigUtils.configsForPrefix(InternalSecretConfigs.PROVIDER_PREFIX, configs);

    String registryGroupId = extensionConfigs.get(InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG);

    return KafkaInternalTopicRepository.create(ConfigUtils.addEntry(
      extensionConfigs,
      InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG,
      String.format("%s-rest", registryGroupId)
    ));
  }
}
