package com.github.mredjem.kafka.connect.oidc.exceptions;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String type) {
    super(String.format("%s could not be found", type));
  }

  public ResourceNotFoundException(String type, String name) {
    super(String.format("%s with name '%s' could not be found", type, name));
  }
}
