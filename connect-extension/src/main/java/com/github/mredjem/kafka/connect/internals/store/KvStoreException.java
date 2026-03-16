package com.github.mredjem.kafka.connect.internals.store;

public class KvStoreException extends RuntimeException {

  public KvStoreException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
