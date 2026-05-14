package com.github.mredjem.kafka.connect.oidc.ccloud;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "create")
public class AsyncCache<T> {

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final AtomicReference<List<T>> cache = new AtomicReference<>();

  private final Supplier<List<T>> supplierFn;

  public void init() {
    this.executor.scheduleWithFixedDelay(this::refreshCache, 0L, 5L, TimeUnit.MINUTES);
  }

  public List<T> getAll() {
    List<T> value = this.cache.get();

    if (value != null) {
      return value;
    }

    this.refreshCache();

    return this.cache.get();
  }

  private void refreshCache() {
    List<T> value = this.supplierFn.get();

    if (value != null) {
      this.cache.set(new ArrayList<>(value));

      return;
    }

    this.cache.set(new ArrayList<>());
  }
}
