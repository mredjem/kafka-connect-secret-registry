package com.github.mredjem.kafka.connect.internals;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaSecretValue {

  private String path;
  private String key;
  private int version;
  private KafkaSecretEncrypted encrypted;
  private String checksum;
  private String createdBy;
  private long createdAt;
}
