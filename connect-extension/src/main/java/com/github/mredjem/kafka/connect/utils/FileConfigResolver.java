package com.github.mredjem.kafka.connect.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Pattern;

@UtilityClass
public class FileConfigResolver {

  private final Pattern SECRETS_PATTERN = Pattern.compile("\\$\\{file:(.+?):(.+?)}");

  public String resolve(String value) {
    return SECRETS_PATTERN.matcher(value)
      .replaceAll(matchResult -> {
        String filename = matchResult.group(1);

        String secret = matchResult.group(2);

        return loadSecretsFile(filename)
          .getOrDefault(secret, matchResult.group())
          .toString();
      });
  }

  @SneakyThrows
  private Properties loadSecretsFile(String filename) {
    String content = Files.readString(Path.of(filename));

    Properties properties = new Properties();
    properties.load(new StringReader(content));

    return properties;
  }
}
