package com.github.mredjem.kafka.connect.internals;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaSecretEncrypted {

  private String derivationInfo;
  private byte[] content;
  private byte[] salt;
  private byte[] iv;
}
