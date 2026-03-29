package com.github.mredjem.kafka.connect.internals.store;

import com.github.mredjem.kafka.connect.internals.KafkaClients;
import com.github.mredjem.kafka.connect.internals.KafkaSecretKey;
import com.github.mredjem.kafka.connect.internals.KafkaSecretValue;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryKvStore implements KvStore<KafkaSecretKey, KafkaSecretValue> {

  private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

  private final Map<KafkaSecretKey, KafkaSecretValue> kvStore = new ConcurrentHashMap<>();

  private final Consumer<KafkaSecretKey, KafkaSecretValue> kvConsumer;

  private volatile boolean running = true;
  private volatile boolean ready;

  private InMemoryKvStore(Map<String, ?> configs) {
    this.kvConsumer = KafkaClients.consumer(configs);
  }

  public static InMemoryKvStore create(Map<String, ?> configs) {
    return new InMemoryKvStore(configs);
  }

  @Override
  public void start(String topic) {
    this.threadPool.submit(() -> {
      try {
        ResetConsumerRebalanceListener rebalanceListener = new ResetConsumerRebalanceListener();

        this.kvConsumer.subscribe(Collections.singletonList(topic), rebalanceListener);

        this.kvConsumer.poll(Duration.ofMillis(100L));
        this.kvConsumer.seekToBeginning(this.kvConsumer.assignment());

        while (this.running) {
          boolean seekToBeginning = rebalanceListener.shouldSeekToBeginning();

          if (seekToBeginning) {
            this.kvConsumer.seekToBeginning(this.kvConsumer.assignment());

            rebalanceListener.resetSeekToBeginningFlag();
          }

          ConsumerRecords<KafkaSecretKey, KafkaSecretValue> consumerRecords = this.kvConsumer.poll(Duration.ofSeconds(1L));

          for (ConsumerRecord<KafkaSecretKey, KafkaSecretValue> record : consumerRecords) {
            if (record.value() == null) {
              this.kvStore.remove(record.key());

              continue;
            }

            this.kvStore.put(record.key(), record.value());
          }

          this.kvConsumer.commitAsync();

          this.evaluateReadiness();
        }

      } catch (final WakeupException e) {
        this.running = false;
      } catch (final Exception e) {
        throw new KvStoreException("Failed to populate key-value store", e);
      } finally {
        try {
          this.kvConsumer.commitSync();
        } finally {
          this.kvConsumer.close();
        }
      }
    });
  }

  @Override
  public synchronized boolean isReady() {
    return this.ready;
  }

  @Override
  public synchronized Optional<KafkaSecretValue> get(KafkaSecretKey key) {
    return Optional.ofNullable(this.kvStore.get(key));
  }

  @Override
  public synchronized Optional<KafkaSecretValue> getLatest(KafkaSecretKey key) {
    return this.kvStore.entrySet()
      .stream()
      .filter(e -> e.getKey().getPath().equals(key.getPath()) && e.getValue().getKey().equals(key.getKey()))
      .max(Comparator.comparingInt(e -> e.getKey().getVersion()))
      .map(Map.Entry::getValue);
  }

  @Override
  public synchronized List<KafkaSecretValue> getAll(Predicate<KafkaSecretKey> filter) {
    return this.kvStore.entrySet()
      .stream()
      .filter(e -> filter.test(e.getKey()))
      .map(Map.Entry::getValue)
      .collect(Collectors.toList());
  }

  @Override
  public void close() {
    this.threadPool.shutdown();

    if (this.kvConsumer != null) {
      this.kvConsumer.wakeup();
    }

    try {
      if (!this.threadPool.isTerminated()) {
        boolean terminated = this.threadPool.awaitTermination(1L, TimeUnit.SECONDS);

        assert terminated;
      }

    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void evaluateReadiness() {
    if (this.ready) {
      return;
    }

    long currentLag = this.computeCurrentLag();

    if (currentLag == 0L) {
      this.ready = true;
    }
  }

  private long computeCurrentLag() {
    return this.kvConsumer.assignment()
      .stream()
      .map(this.kvConsumer::currentLag)
      .map(lag -> lag.orElse(0L))
      .mapToLong(Long::longValue)
      .sum();
  }

  private static class ResetConsumerRebalanceListener implements ConsumerRebalanceListener {

    private boolean shouldSeekToBeginning = false;

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
      // noop
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
      this.shouldSeekToBeginning = true;
    }

    public boolean shouldSeekToBeginning() {
      return shouldSeekToBeginning;
    }

    public void resetSeekToBeginningFlag() {
      this.shouldSeekToBeginning = false;
    }
  }
}
