package com.github.mredjem.kafka.connect.utils;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public class ConfigUtils {

  public Map<String, String> configsForPrefix(String prefix, Map<String, ?> configs) {
    Map<String, String> newConfigs = new HashMap<>();

    for (Map.Entry<String, ?> config : configs.entrySet()) {
      if (config.getKey().startsWith(prefix)) {
        String newKey = config.getKey().substring(prefix.length());

        newConfigs.put(newKey, (String) config.getValue());
      }
    }

    return newConfigs;
  }

  public String getOrThrow(String key, Map<String, ?> configs) {
    String value = (String) configs.get(key);

    if (value == null || value.trim().isEmpty()) {
      throw new MissingRequiredConfigException(key);
    }

    return value;
  }

  public Integer getInt(String key, Map<String, ?> configs) {
    String value = getOrThrow(key, configs);

    return Integer.parseInt(value);
  }

  public Properties toProperties(Map<String, ?> configs) {
    Properties props = new Properties();

    for (Map.Entry<String, ?> config : configs.entrySet()) {
      props.setProperty(config.getKey(), config.getValue().toString());
    }

    return props;
  }

  public Map<String, String> addEntry(Map<String, String> map, String key, String value) {
    Map<String, String> resultMap = new HashMap<>(map);

    resultMap.put(key, value);

    return resultMap;
  }
}
