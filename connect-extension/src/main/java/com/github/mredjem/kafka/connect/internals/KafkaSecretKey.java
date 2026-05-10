package com.github.mredjem.kafka.connect.internals;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class KafkaSecretKey {

  private String keyType = "SECRET";
  private String path;
  private int version;
  private String key;
}
