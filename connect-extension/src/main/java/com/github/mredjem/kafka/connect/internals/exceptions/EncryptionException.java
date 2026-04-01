package com.github.mredjem.kafka.connect.internals.exceptions;

public class EncryptionException extends RuntimeException {

  public EncryptionException(String message, Throwable cause) {
    super(message, cause);
  }
}
