package com.github.mredjem.kafka.connect.internals;

import com.github.mredjem.kafka.connect.internals.exceptions.KafkaInternalTopicOperationException;
import com.github.mredjem.kafka.connect.internals.mappers.KafkaSecretKeyMapper;
import com.github.mredjem.kafka.connect.internals.mappers.KafkaSecretValueMapper;
import com.github.mredjem.kafka.connect.internals.store.InMemoryKvStore;
import com.github.mredjem.kafka.connect.internals.store.KvStore;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.awaitility.Awaitility;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.github.mredjem.kafka.connect.internals.KafkaInternalTopicConstants.ALL;
import static com.github.mredjem.kafka.connect.internals.KafkaInternalTopicConstants.LATEST;
import static com.github.mredjem.kafka.connect.internals.KafkaInternalTopicConstants.LATEST_VERSION;
import static com.github.mredjem.kafka.connect.internals.KafkaInternalTopicConstants.SEARCH_KEYWORDS;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.KAFKASTORE_TOPIC_CONFIG;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.KAFKASTORE_TOPIC_REPLICATION_FACTOR_CONFIG;
import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.MASTER_ENCRYPTION_KEY_CONFIG;

public class KafkaInternalTopicClient implements Closeable {

  private final Map<String, ?> configs;

  private final KvStore<KafkaSecretKey, KafkaSecretValue> kvStore;

  private KafkaInternalTopicClient(Map<String, ?> configs) {
    this.configs = new HashMap<>(configs);
    this.kvStore = InMemoryKvStore.create(configs);
  }

  public static KafkaInternalTopicClient create(Map<String, ?> configs) {
    return new KafkaInternalTopicClient(configs);
  }

  public void init() throws ExecutionException, InterruptedException {
    String topicName = ConfigUtils.getOrThrow(KAFKASTORE_TOPIC_CONFIG, this.configs);

    try (AdminClient adminClient = KafkaClients.adminClient(this.configs)) {
      ListTopicsOptions options = new ListTopicsOptions().listInternal(false);

      Set<String> topicNames = adminClient.listTopics(options).names().getNow(new HashSet<>());

      if (!topicNames.contains(topicName)) {
        try {
          int replicationFactor = ConfigUtils.getInt(KAFKASTORE_TOPIC_REPLICATION_FACTOR_CONFIG, this.configs);

          Map<String, String> cleanupPolicy = Collections.singletonMap(
            TopicConfig.CLEANUP_POLICY_CONFIG,
            TopicConfig.CLEANUP_POLICY_COMPACT
          );

          NewTopic internalTopic = new NewTopic(topicName, 1, (short) replicationFactor).configs(cleanupPolicy);

          adminClient.createTopics(Collections.singletonList(internalTopic)).all().get();

        } catch (final ExecutionException e) {
          if (!e.getMessage().contains(String.format("Topic '%s' already exists", topicName))) {
            throw new KafkaInternalTopicOperationException("create", e);
          }
        }
      }

      this.kvStore.start(topicName);

      Awaitility.await().atMost(15L, TimeUnit.SECONDS).until(this.kvStore::isReady);
    }
  }

  public int saveNewSecret(String path, String key, int version, String secret) {
    String topicName = ConfigUtils.getOrThrow(KAFKASTORE_TOPIC_CONFIG, this.configs);

    try (Producer<KafkaSecretKey, KafkaSecretValue> producer = KafkaClients.producer(this.configs)) {
      KafkaSecretKey kafkaSecretKey = KafkaSecretKeyMapper.newKey(path, key, version);

      KafkaSecretValue kafkaSecretValue = this.newValue(
        kafkaSecretKey.getPath(),
        kafkaSecretKey.getKey(),
        kafkaSecretKey.getVersion(),
        secret
      );

      ProducerRecord<KafkaSecretKey, KafkaSecretValue> newSecret = new ProducerRecord<>(topicName, kafkaSecretKey, kafkaSecretValue);

      producer.send(newSecret).get(5L, TimeUnit.SECONDS);

      return kafkaSecretKey.getVersion();

    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new KafkaInternalTopicOperationException("produce to", e);

    } catch (final Exception e) {
      throw new KafkaInternalTopicOperationException("produce to", e);
    }
  }

  public void deleteSecret(String path, String key, int version) {
    String topicName = ConfigUtils.getOrThrow(KAFKASTORE_TOPIC_CONFIG, this.configs);

    try (Producer<KafkaSecretKey, KafkaSecretValue> producer = KafkaClients.producer(this.configs)) {
      KafkaSecretKey kafkaSecretKey = KafkaSecretKeyMapper.newKey(path, key, version);

      ProducerRecord<KafkaSecretKey, KafkaSecretValue> tombstone = new ProducerRecord<>(topicName, kafkaSecretKey, null);

      producer.send(tombstone).get(5L, TimeUnit.SECONDS);

    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new KafkaInternalTopicOperationException("delete from", e);

    } catch (final Exception e) {
      throw new KafkaInternalTopicOperationException("delete from", e);
    }
  }

  public List<KafkaSecretValue> searchForSecrets(String path, String key, String version) {
    if (SEARCH_KEYWORDS.contains(path) || SEARCH_KEYWORDS.contains(key) || SEARCH_KEYWORDS.contains(version)) {
      Predicate<KafkaSecretKey> searchPredicate = this.kafkaSecretKeyPredicate(path, key, version);

      return this.kvStore.getAll(searchPredicate);
    }

    KafkaSecretKey kafkaSecretKey = KafkaSecretKeyMapper.newKey(path, key, Integer.parseInt(version));

    return this.kvStore.get(kafkaSecretKey)
      .map(Arrays::asList)
      .orElse(Collections.emptyList());
  }

  @Override
  public void close() throws IOException {
    if (this.kvStore != null) {
      this.kvStore.close();
    }
  }

  private KafkaSecretValue newValue(String path, String key, int version, String secret) {
    String masterKey = ConfigUtils.getOrThrow(MASTER_ENCRYPTION_KEY_CONFIG, this.configs);

    return KafkaSecretValueMapper
      .create(masterKey)
      .newValue(path, key, version, secret);
  }

  private Predicate<KafkaSecretKey> kafkaSecretKeyPredicate(String path, String key, String version) {
    return secretKey -> {
      if (ALL.equals(path) && ALL.equals(key) && ALL.equals(version)) {
        return true;
      }

      if (!ALL.equals(path) && !secretKey.getPath().equals(path)) {
        return false;
      }

      if (ALL.equals(key) && ALL.equals(version)) {
        return true;
      }

      if (!ALL.equals(key) && !secretKey.getKey().equals(key)) {
        return false;
      }

      if (ALL.equals(version)) {
        return true;
      }

      if (LATEST.equals(version)) {
        KafkaSecretKey latestKey = KafkaSecretKeyMapper.newKey(path, key, LATEST_VERSION);

        int latestVersion = this.kvStore.getLatest(latestKey)
          .map(KafkaSecretValue::getVersion)
          .orElse(LATEST_VERSION);

        return secretKey.getVersion() == latestVersion;
      }

      return Integer.toString(secretKey.getVersion()).equals(version);
    };
  }
}
