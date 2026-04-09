package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApi;
import com.github.mredjem.kafka.connect.extensions.filters.BasicAuthFilter;
import com.github.mredjem.kafka.connect.extensions.filters.BearerAuthFilter;
import com.github.mredjem.kafka.connect.internals.KafkaAuthorizationRepository;
import com.github.mredjem.kafka.connect.internals.KafkaInternalTopicRepository;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.azure.EntraIDRepository;
import com.github.mredjem.kafka.connect.oidc.azure.managed.ConfluentCloudRepository;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigs;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.Map;

public class SecretRegistryExtension implements ConnectRestExtension {

  private SecretRegistryPort secretRegistryPort;
  private ContainerRequestFilter containerRequestFilter;

  @Override
  public String version() {
    return "1";
  }

  @Override
  public void configure(Map<String, ?> configs) {
    this.doConfigureSecretRegistryPort(configs);
    this.doConfigureSecretRegistryFilter(configs);
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

  private void doConfigureSecretRegistryPort(Map<String, ?> configs) {
    Map<String, String> extensionConfigs = this.getExtensionConfigs(configs);

    String registryGroupId = extensionConfigs.get(InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG);

    this.secretRegistryPort = KafkaInternalTopicRepository.create(ConfigUtils.addEntry(
      extensionConfigs,
      InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG,
      String.format("%s-rest", registryGroupId)
    ));
  }

  private void doConfigureSecretRegistryFilter(Map<String, ?> configs) {
    Map<String, String> extensionConfigs = this.getExtensionConfigs(configs);

    OidcPort oidcPort = this.getOidcImplementation(configs, extensionConfigs);

    this.containerRequestFilter = BasicAuthFilter.create(
      extensionConfigs,
      BearerAuthFilter.create(KafkaAuthorizationRepository.create(oidcPort))
    );
  }

  private OidcPort getOidcImplementation(Map<String, ?> configs, Map<String, String> extensionConfigs) {
    Map<String, String> oidcConfigs = ConfigUtils.getConfigsForPrefix(OidcConfigs.OIDC_PREFIX, configs);

    if (oidcConfigs.isEmpty()) {
      Map<String, String> kafkaStoreConfigs = ConfigUtils.getConfigsForPrefix(InternalSecretConfigs.KAFKASTORE_PREFIX, extensionConfigs);

      return EntraIDRepository.create(kafkaStoreConfigs);
    }

    return ConfluentCloudRepository.create(oidcConfigs);
  }

  private Map<String, String> getExtensionConfigs(Map<String, ?> configs) {
    return ConfigUtils.getConfigsForPrefix(
      String.format(
        InternalSecretConfigs.PROVIDER_PREFIX,
        InternalSecretConfigs.PROVIDER_NAME
      ),
      configs
    );
  }
}
