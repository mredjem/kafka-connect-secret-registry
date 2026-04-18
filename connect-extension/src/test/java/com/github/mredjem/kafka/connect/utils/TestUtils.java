package com.github.mredjem.kafka.connect.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class TestUtils {

  private static final String CONNECT_CONFIG_PATH = "etc/kafka/connect-distributed.properties";

  private TestUtils() {}

  public static Map<String, String> load() {
    try (InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(CONNECT_CONFIG_PATH)) {
      assert is != null;

      Properties properties = new Properties();

      properties.load(is);

      return convertMap(properties);

    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Map<String, String> convertMap(Map<Object, Object> map) {
    Map<String, String> copy = new HashMap<>();

    for (Map.Entry<Object, Object> e : map.entrySet()) {
      copy.put(e.getKey().toString(), e.getValue().toString());
    }

    return copy;
  }
}
