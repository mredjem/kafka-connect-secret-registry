package com.github.mredjem.kafka.connect.internals.store;

public interface WaitingConsumer {

  void start(String topic);

  boolean isReady();
}
