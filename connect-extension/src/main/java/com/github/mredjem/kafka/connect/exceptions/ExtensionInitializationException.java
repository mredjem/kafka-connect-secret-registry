package com.github.mredjem.kafka.connect.exceptions;

public class ExtensionInitializationException extends RuntimeException {

  public ExtensionInitializationException(Throwable cause) {
    super("Extension failed to initialize", cause);
  }
}
