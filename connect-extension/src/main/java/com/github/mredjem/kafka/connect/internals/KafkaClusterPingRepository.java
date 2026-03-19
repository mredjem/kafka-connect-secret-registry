package com.github.mredjem.kafka.connect.internals;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.internals.callbacks.StaticTokenCallbackHandlerCallback;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.github.mredjem.kafka.connect.internals.callbacks.StaticTokenCallbackHandlerCallback.SASL_OAUTHBEARER_ACCESS_TOKEN_CONFIG;

public class KafkaClusterPingRepository implements AuthorizationPort {

  private final Properties configs;

  private KafkaClusterPingRepository(Map<String, String> configs) {
    this.configs = ConfigUtils.toProperties(configs);
  }

  public static KafkaClusterPingRepository create(Map<String, String> configs) {
    return new KafkaClusterPingRepository(configs);
  }

  @Override
  public boolean checkAccess(AuthenticationCredentials authenticationCredentials) {
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

  private Properties getBearerConfigs(AuthenticationCredentials authenticationCredentials) {
    Properties bearerConfigs = ConfigUtils.copyProperties(this.configs);

    if (this.configs.containsKey(SaslConfigs.SASL_JAAS_CONFIG)) {
      bearerConfigs.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
      bearerConfigs.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, StaticTokenCallbackHandlerCallback.class);
      bearerConfigs.put(SASL_OAUTHBEARER_ACCESS_TOKEN_CONFIG, authenticationCredentials.getCredentials());
    }

    return bearerConfigs;
  }
}
