package com.github.mredjem.kafka.connect.extensions.rbac;

import com.github.mredjem.kafka.connect.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class RequestedAction {

  private final Operation operation;
  private final String resourceName;
}
