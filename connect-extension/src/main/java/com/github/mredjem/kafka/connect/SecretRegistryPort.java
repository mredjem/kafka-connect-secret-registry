package com.github.mredjem.kafka.connect;

import java.io.Closeable;
import java.util.Optional;
import java.util.Set;

public interface SecretRegistryPort extends Closeable {

  Set<Path> getPaths();

  Set<Key> getKeys(String path);

  Set<Version> getVersions(String path, String key);

  Set<Secret> getSecrets(String path, Set<String> keys);

  Set<Secret> getSecrets(String path, String key);

  Optional<Secret> getSecret(String path, String key, String version);

  Secret createSecret(String path, String key, String secret);

  void deleteSecret(String path, String key, String version);

  void deleteKey(String path, String key);

  void deletePath(String path);
}
