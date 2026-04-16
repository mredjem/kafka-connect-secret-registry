package com.github.mredjem.kafka.connect.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ConfigUtils {

  private ConfigUtils() {}

  public static Map<String, String> configsForPrefix(String prefix, Map<String, ?> configs) {
    Map<String, String> newConfigs = new HashMap<>();

    for (Map.Entry<String, ?> config : configs.entrySet()) {
      if (config.getKey().startsWith(prefix)) {
        String newKey = config.getKey().substring(prefix.length());

        newConfigs.put(newKey, (String) config.getValue());
      }
    }

    return newConfigs;
  }

  public static String getOrThrow(String key, Map<String, ?> configs) {
    String value = (String) configs.get(key);

    if (value == null || value.trim().isEmpty()) {
      throw new MissingRequiredConfigException(key);
    }

    return value;
  }

  public static Integer getInt(String key, Map<String, ?> configs) {
    String value = getOrThrow(key, configs);

    return Integer.parseInt(value);
  }

  public static Properties toProperties(Map<String, ?> configs) {
    Properties props = new Properties();

    for (Map.Entry<String, ?> config : configs.entrySet()) {
      props.setProperty(config.getKey(), config.getValue().toString());
    }

    return props;
  }

  public static Properties copyProperties(Properties props) {
    Properties newProps = new Properties();

    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      newProps.setProperty(entry.getKey().toString(), entry.getValue().toString());
    }

    return newProps;
  }

  public static Map<String, String> addEntry(Map<String, String> map, String key, String value) {
    Map<String, String> resultMap = new HashMap<>(map);

    resultMap.put(key, value);

    return resultMap;
  }
}
