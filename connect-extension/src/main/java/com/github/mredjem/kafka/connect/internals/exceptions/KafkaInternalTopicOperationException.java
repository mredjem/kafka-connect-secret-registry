package com.github.mredjem.kafka.connect.internals.exceptions;

public class KafkaInternalTopicOperationException extends RuntimeException {

  public KafkaInternalTopicOperationException(String operation, Throwable cause) {
    super(String.format("Failed to %s internal topic", operation), cause);
  }
}
