package com.github.mredjem.kafka.connect.internals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.roles.ConnectManager;
import com.github.mredjem.kafka.connect.roles.DeveloperRead;
import com.github.mredjem.kafka.connect.roles.DeveloperWrite;
import com.github.mredjem.kafka.connect.roles.EnvironmentAdmin;
import com.github.mredjem.kafka.connect.roles.Operator;
import com.github.mredjem.kafka.connect.roles.ResourceOwner;
import com.github.mredjem.kafka.connect.Role;
import com.github.mredjem.kafka.connect.internals.callbacks.StaticTokenCallbackHandlerCallback;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import com.github.mredjem.kafka.connect.internals.utils.JwtUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KafkaAuthorizationRepository implements AuthorizationPort {

  private static final String ROLE_PREFIX = "KafkaConnect.";

  private static final Map<String, Role> ROLES_BY_NAME;

  static {
    List<Role> roles = new ArrayList<>();

    roles.add(new DeveloperRead());
    roles.add(new DeveloperWrite());
    roles.add(new Operator());
    roles.add(new ConnectManager());
    roles.add(new ResourceOwner());
    roles.add(new EnvironmentAdmin());

    ROLES_BY_NAME = roles.stream().collect(Collectors.toMap(role -> ROLE_PREFIX + role.roleName(), Function.identity()));
  }

  private final Properties configs;

  private KafkaAuthorizationRepository(Map<String, String> configs) {
    this.configs = ConfigUtils.toProperties(configs);
  }

  public static KafkaAuthorizationRepository create(Map<String, String> configs) {
    return new KafkaAuthorizationRepository(configs);
  }

  @Override
  public boolean validateToken(AuthenticationCredentials authenticationCredentials) {
    Properties adminConfigs = AuthenticationKind.BEARER == authenticationCredentials.getKind() ? this.getBearerConfigs(authenticationCredentials) : this.configs;

    try (AdminClient adminClient = AdminClient.create(adminConfigs)) {
      String clusterId = adminClient.describeCluster()
        .clusterId()
        .get(5L, TimeUnit.SECONDS);

      assert clusterId != null;

      return true;

    } catch (final Exception ignored) {
      return false;
    }
  }

  @Override
  public List<Role> getAppRoles(AuthenticationCredentials authenticationCredentials) {
    try {
      List<String> roles = JwtUtils.getClaimsAsList(authenticationCredentials.getCredentials(), "roles");

      return roles.stream()
        .map(ROLES_BY_NAME::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    } catch (final JsonProcessingException ignored) {
      return Collections.emptyList();
    }
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
