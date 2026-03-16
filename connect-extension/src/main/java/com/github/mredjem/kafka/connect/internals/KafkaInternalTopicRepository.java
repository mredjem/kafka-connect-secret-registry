package com.github.mredjem.kafka.connect.internals;

import com.github.mredjem.kafka.connect.Key;
import com.github.mredjem.kafka.connect.Path;
import com.github.mredjem.kafka.connect.Secret;
import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.Version;
import com.github.mredjem.kafka.connect.exceptions.ExtensionInitializationException;
import com.github.mredjem.kafka.connect.internals.mappers.SecretMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.mredjem.kafka.connect.internals.KafkaInternalTopicConstants.ALL;
import static com.github.mredjem.kafka.connect.internals.KafkaInternalTopicConstants.LATEST;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.MASTER_ENCRYPTION_KEY_CONFIG;

public class KafkaInternalTopicRepository implements SecretRegistryPort {

  private final KafkaInternalTopicClient kafkaInternalTopicClient;

  private final SecretMapper secretMapper;

  private KafkaInternalTopicRepository(Map<String, ?> configs) {
    try {
      this.kafkaInternalTopicClient = KafkaInternalTopicClient.create(configs);
      this.kafkaInternalTopicClient.init();

      this.secretMapper = SecretMapper.create((String) configs.get(MASTER_ENCRYPTION_KEY_CONFIG));

    } catch (final Exception e) {
      throw new ExtensionInitializationException(e);
    }
  }

  public static KafkaInternalTopicRepository create(Map<String, ?> configs) {
    return new KafkaInternalTopicRepository(configs);
  }

  @Override
  public Set<Path> getPaths() {
    return this.kafkaInternalTopicClient.searchForSecrets(ALL, ALL, ALL)
      .stream()
      .map(KafkaSecretValue::getPath)
      .map(Path::of)
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Key> getKeys(String path) {
    return this.kafkaInternalTopicClient.searchForSecrets(path, ALL, ALL)
      .stream()
      .map(KafkaSecretValue::getKey)
      .map(key -> Key.of(path, key))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Version> getVersions(String path, String key) {
    return this.kafkaInternalTopicClient.searchForSecrets(path, key, ALL)
      .stream()
      .map(KafkaSecretValue::getVersion)
      .map(version -> Version.of(path, key, version))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Secret> getSecrets(String path, Set<String> keys) {
    return keys.stream()
      .map(key -> this.getSecret(path, key, LATEST).orElse(null))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Secret> getSecrets(String path, String key) {
    return this.kafkaInternalTopicClient.searchForSecrets(path, key, ALL)
      .stream()
      .map(this.secretMapper::newSecret)
      .collect(Collectors.toSet());
  }

  @Override
  public Optional<Secret> getSecret(String path, String key, String version) {
    List<KafkaSecretValue> secretValues = this.kafkaInternalTopicClient.searchForSecrets(path, key, version);

    if (secretValues.isEmpty()) {
      return Optional.empty();
    }

    Secret secret = this.secretMapper.newSecret(secretValues.get(0));

    return Optional.of(secret);
  }

  @Override
  public Secret createSecret(String path, String key, String secret) {
    Version nextVersion = this.getSecret(path, key, LATEST)
      .map(e -> e.getVersion().nextVersion())
      .orElse(Version.init(path, key));

    int newVersion = this.kafkaInternalTopicClient.saveNewSecret(path, key, nextVersion.getVersion(), secret);

    return Secret.of(path, key, newVersion, secret);
  }

  @Override
  public void deleteSecret(String path, String key, String version) {
    List<KafkaSecretValue> secretValues = this.kafkaInternalTopicClient.searchForSecrets(path, key, version);

    for (KafkaSecretValue secretValue : secretValues) {
      this.kafkaInternalTopicClient.deleteSecret(
        secretValue.getPath(),
        secretValue.getKey(),
        secretValue.getVersion()
      );
    }
  }

  @Override
  public void deleteKey(String path, String key) {
    this.deleteSecret(path, key, ALL);
  }

  @Override
  public void deletePath(String path) {
    this.deleteKey(path, ALL);
  }

  @Override
  public void close() throws IOException {
    this.kafkaInternalTopicClient.close();
  }
}
