package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.extensions.filters.AuthenticationFilter;
import com.github.mredjem.kafka.connect.extensions.filters.RbacAuthenticationFilter;
import com.github.mredjem.kafka.connect.internals.KafkaAuthorizationRepository;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.ccloud.ConfluentCloudRepository;
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
    Map<String, String> oidcConfigs = ConfigUtils.configsForPrefix(OidcConfigs.OIDC_PREFIX, configs);

    AuthorizationPort authorizationPort = KafkaAuthorizationRepository.create(ConfluentCloudRepository.create(oidcConfigs));

    ContainerRequestFilter rbacAuthenticationFilter = RbacAuthenticationFilter.create(authorizationPort);

    this.authenticationFilter = AuthenticationFilter.create(rbacAuthenticationFilter);
  }
}
