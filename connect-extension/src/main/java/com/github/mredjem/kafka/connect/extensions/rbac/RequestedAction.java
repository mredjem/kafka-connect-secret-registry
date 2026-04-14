package com.github.mredjem.kafka.connect.extensions.rbac;

import com.github.mredjem.kafka.connect.Operation;

public class RequestedAction {

  private final Operation operation;

  private final String resourceName;

  private RequestedAction(Operation operation, String resourceName) {
    this.operation = operation;
    this.resourceName = resourceName;
  }

  public static RequestedAction of(Operation operation, String resourceName) {
    return new RequestedAction(operation, resourceName);
  }

  public Operation getOperation() {
    return this.operation;
  }

  public String getResourceName() {
    return this.resourceName;
  }
}
