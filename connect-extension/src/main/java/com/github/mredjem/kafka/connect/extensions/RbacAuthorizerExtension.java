package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.extensions.filters.AuthenticationFilter;
import com.github.mredjem.kafka.connect.internals.KafkaAuthorizationRepository;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.azure.EntraIDRepository;
import com.github.mredjem.kafka.connect.oidc.azure.ccloud.ConfluentCloudRepository;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigs;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

import javax.ws.rs.container.ContainerRequestFilter;
import java.util.Map;

public class RbacAuthorizerExtension implements ConnectRestExtension {

  private ContainerRequestFilter authenticationFilter;

  @Override
  public String version() {
    return "1";
  }

  @Override
  public void configure(Map<String, ?> configs) {
    this.configureAuthenticationFilter(configs);
  }

  @Override
  public void register(ConnectRestExtensionContext restPluginContext) {
    restPluginContext.configurable().register(this.authenticationFilter);
  }

  @Override
  public void close() {
    // noop
  }

  private void configureAuthenticationFilter(Map<String, ?> configs) {
    Map<String, String> extensionConfigs = ConfigUtils.configsForPrefix(InternalSecretConfigs.PROVIDER_PREFIX, configs);

    OidcPort oidcPort = this.getOidcImplementation(configs, extensionConfigs);

    this.authenticationFilter = AuthenticationFilter.create(extensionConfigs, KafkaAuthorizationRepository.create(oidcPort));
  }

  private OidcPort getOidcImplementation(Map<String, ?> configs, Map<String, String> extensionConfigs) {
    Map<String, String> oidcConfigs = ConfigUtils.configsForPrefix(OidcConfigs.OIDC_PREFIX, configs);

    if (oidcConfigs.isEmpty()) {
      Map<String, String> kafkaStoreConfigs = ConfigUtils.configsForPrefix(InternalSecretConfigs.KAFKASTORE_PREFIX, extensionConfigs);

      return EntraIDRepository.create(kafkaStoreConfigs);
    }

    return ConfluentCloudRepository.create(oidcConfigs);
  }
}
