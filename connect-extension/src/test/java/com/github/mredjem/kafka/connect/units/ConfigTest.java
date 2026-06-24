package com.github.mredjem.kafka.connect.units;

import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

class ConfigTest {

  @Test
  void secretsShouldBeResolved() {
    URL url = Objects.requireNonNull(ConfigTest.class.getClassLoader().getResource("etc/kafka/plain.txt"));

    String filename = url.getPath();

    Map<String, ?> configs = Map.of(
      "sasl.jaas.config",
      "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${file:" + filename + ":username}\" password=\"${file:" + filename + ":password}\";"
    );

    Map<String, ?> resolved = ConfigUtils.resolveSecrets(configs);

    String expected = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"ABCDEFGHIJKLMNOP\" password=\"R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987\";";

    Assertions.assertEquals(expected, resolved.get("sasl.jaas.config"));
  }
}
