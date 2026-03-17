package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApi;
import com.github.mredjem.kafka.connect.extensions.filters.BasicAuthFilter;
import com.github.mredjem.kafka.connect.extensions.filters.BearerAuthFilter;
import com.github.mredjem.kafka.connect.internals.KafkaInternalTopicRepository;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.Map;

import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.PROVIDER_NAME;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.PROVIDER_PREFIX;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG;

public class SecretRegistryExtension implements ConnectRestExtension {

  private SecretRegistryPort secretRegistryPort;

  private ContainerRequestFilter containerRequestFilter;

  @Override
  public String version() {
    return "1";
  }

  @Override
  public void configure(Map<String, ?> configs) {
    Map<String, String> extensionConfigs = ConfigUtils.getConfigsForPrefix(String.format(PROVIDER_PREFIX, PROVIDER_NAME), configs);

    String registryGroupId = extensionConfigs.get(SECRET_REGISTRY_GROUP_ID_CONFIG);

    this.secretRegistryPort = KafkaInternalTopicRepository.create(ConfigUtils.addEntry(
      extensionConfigs,
      SECRET_REGISTRY_GROUP_ID_CONFIG,
      String.format("%s-rest", registryGroupId)
    ));

    this.containerRequestFilter = BasicAuthFilter.create(extensionConfigs, BearerAuthFilter.create(extensionConfigs));
  }

  @Override
  public void register(ConnectRestExtensionContext restPluginContext) {
    restPluginContext.configurable()
      .register(this.containerRequestFilter)
      .register(SecretRegistryApi.create(this.secretRegistryPort));
  }

  @Override
  public void close() throws IOException {
    if (this.secretRegistryPort != null) {
      this.secretRegistryPort.close();
    }
  }
}
