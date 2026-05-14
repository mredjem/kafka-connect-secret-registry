package com.github.mredjem.kafka.connect.oidc.ccloud;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "create")
public class AsyncCache<T> {

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final AtomicReference<T> cache = new AtomicReference<>();

  private final Supplier<T> supplierFn;

  public void init() {
    Runnable runnable = () -> {
      T value = this.supplierFn.get();

      this.cache.set(value);
    };

    this.executor.scheduleWithFixedDelay(runnable, 0L, 5L, TimeUnit.MINUTES);
  }

  public T getAll() {
    return this.cache.get();
  }
}
