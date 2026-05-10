package com.github.mredjem.kafka.connect.internals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class KafkaInternalTopicConstants {

  public static final String ALL = "*";
  public static final String LATEST = "latest";
  public static final int LATEST_VERSION = -1;

  static final Set<String> SEARCH_KEYWORDS = new HashSet<>(Arrays.asList(ALL, LATEST));

  private KafkaInternalTopicConstants() {}
}
