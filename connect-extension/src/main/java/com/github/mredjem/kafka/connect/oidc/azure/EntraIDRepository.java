package com.github.mredjem.kafka.connect.oidc.azure;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.RoleBinding;
import com.github.mredjem.kafka.connect.Scope;
import com.github.mredjem.kafka.connect.internals.callbacks.StaticTokenCallbackHandlerCallback;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.Roles;
import com.github.mredjem.kafka.connect.oidc.utils.JwtUtils;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EntraIDRepository implements OidcPort {

  private static final String ROLE_PREFIX = "^KafkaConnect\\.";

  private final Properties configs;

  private EntraIDRepository(Map<String, String> configs) {
    this.configs = ConfigUtils.toProperties(configs);
  }

  public static EntraIDRepository create(Map<String, String> configs) {
    return new EntraIDRepository(configs);
  }

  @Override
  public boolean validateCredentials(AuthenticationCredentials authenticationCredentials) {
    Properties adminConfigs = this.getBearerConfigs(authenticationCredentials);

    try (AdminClient adminClient = AdminClient.create(adminConfigs)) {
      String clusterId = adminClient.describeCluster()
        .clusterId()
        .get(5L, TimeUnit.SECONDS);

      return clusterId != null && !clusterId.isEmpty();

    } catch (final Exception ignored) {
      return false;
    }
  }

  @Override
  public List<RoleBinding> getRoleBindings(AuthenticationCredentials authenticationCredentials) {
    List<String> roles = JwtUtils.getClaimAsList(authenticationCredentials.getCredentials(), "roles");

    return roles.stream()
      .map(roleName -> Roles.getRoles().get(roleName.replaceFirst(ROLE_PREFIX, "")))
      .filter(Objects::nonNull)
      .map(role -> RoleBinding.of(role, Scope.ALL))
      .collect(Collectors.toList());
  }

  private Properties getBearerConfigs(AuthenticationCredentials authenticationCredentials) {
    Properties bearerConfigs = ConfigUtils.copyProperties(this.configs);

    if (this.configs.containsKey(SaslConfigs.SASL_JAAS_CONFIG)) {
      bearerConfigs.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
      bearerConfigs.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, StaticTokenCallbackHandlerCallback.class);
      bearerConfigs.put(StaticTokenCallbackHandlerCallback.SASL_OAUTHBEARER_ACCESS_TOKEN_CONFIG, authenticationCredentials.getCredentials());
    }

    return bearerConfigs;
  }
}
