package com.github.mredjem.kafka.connect.providers;

import com.github.mredjem.kafka.connect.Secret;
import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.internals.KafkaInternalTopicRepository;
import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.provider.ConfigProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InternalSecretConfigProvider implements ConfigProvider {

  private SecretRegistryPort secretRegistryPort;

  @Override
  public void configure(Map<String, ?> configs) {
    this.secretRegistryPort = KafkaInternalTopicRepository.create(configs);
  }

  @Override
  public ConfigData get(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ConfigData get(String path, Set<String> keys) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Config path cannot be null or empty");
    }

    Map<String, String> secrets = this.secretRegistryPort.getSecrets(path, keys)
      .stream()
      .collect(Collectors.toMap(
        secret -> secret.getVersion().getKey().getKey(),
        Secret::getSecret
      ));

    return new ConfigData(secrets);
  }

  @Override
  public void close() throws IOException {
    if (this.secretRegistryPort != null) {
      this.secretRegistryPort.close();
    }
  }
}
