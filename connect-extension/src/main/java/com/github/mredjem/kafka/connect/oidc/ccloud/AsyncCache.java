package com.github.mredjem.kafka.connect.oidc.ccloud;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AsyncCache<T> {

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final AtomicReference<List<T>> cache = new AtomicReference<>();

  private final Supplier<List<T>> refreshFn;

  public static <T> AsyncCache<T> create(Supplier<List<T>> refreshFn) {
    return new AsyncCache<>(refreshFn).init();
  }

  public List<T> getAll() {
    List<T> actual = cache.get();

    if (actual != null) {
      return actual;
    }

    this.refreshCache();

    List<T> fetched = cache.get();

    return fetched != null ? fetched : new ArrayList<>();
  }

  private AsyncCache<T> init() {
    this.executor.scheduleWithFixedDelay(this::refreshCache, 0L, 5L, TimeUnit.MINUTES);

    return this;
  }

  private void refreshCache() {
    try {
      List<T> value = this.refreshFn.get();

      if (value != null) {
        this.cache.set(new ArrayList<>(value));

        return;
      }

      this.cache.set(new ArrayList<>());

    } catch (final Exception e) {
      log.warn("Failed to refresh cache", e);
    }
  }
}
