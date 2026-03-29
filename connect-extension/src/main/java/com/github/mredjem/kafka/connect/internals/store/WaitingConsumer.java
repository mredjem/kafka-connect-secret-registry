package com.github.mredjem.kafka.connect.internals.store;

public interface WaitingForConsumerToStart {

  void startConsumer(String topic);

  boolean isReady();
}
