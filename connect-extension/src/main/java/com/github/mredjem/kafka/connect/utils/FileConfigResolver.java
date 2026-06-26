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

  private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

  private final Pattern SECRETS_PATTERN = Pattern.compile("\\$\\{file:(.+?)}");

  public String resolve(String value) {
    return SECRETS_PATTERN.matcher(value)
      .replaceAll(matchResult -> {
        String secretPath = matchResult.group(1);

        int separatorIdx = secretPath.lastIndexOf(":");

        if (separatorIdx == -1) {
          return matchResult.group();
        }

        String filename = formatFilename(secretPath.substring(0, separatorIdx));

        String secretName = secretPath.substring(separatorIdx + 1);

        return loadSecretsFile(filename)
          .getOrDefault(secretName, matchResult.group())
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

  private String formatFilename(String filename) {
    return isWindows() ? filename.substring(1) : filename;
  }

  private boolean isWindows() {
    return OS_NAME.startsWith("windows");
  }
}
