package com.github.mredjem.kafka.connect.internals.serdes;

public class DeSerializationException extends RuntimeException {

  public DeSerializationException(String action, boolean key, Throwable cause) {
    super(String.format("Failed to %s the %s part of the message", action, key ? "key" : "value"), cause);
  }
}
