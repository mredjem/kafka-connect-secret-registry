package com.github.mredjem.kafka.connect.internals.store;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface KvStore<K, V> extends Closeable, WaitingForConsumerToStart {

  Optional<V> get(K key);

  Optional<V> getLatest(K key);

  List<V> getAll(Predicate<K> filter);
}
