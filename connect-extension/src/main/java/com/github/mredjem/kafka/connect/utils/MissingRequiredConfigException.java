package com.github.mredjem.kafka.connect.utils;

public class MissingRequiredConfigException extends RuntimeException {

  public MissingRequiredConfigException(String configurationKey) {
    super(String.format("'%s' is required and missing from configuration", configurationKey));
  }
}
