package com.github.mredjem.kafka.connect.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ConfluentKafkaConnectContainer extends GenericContainer<ConfluentKafkaConnectContainer> {

  public static final String PLUGIN_PATH = "/etc/kafka-connect/jars";

  private static final File CONNECT_EXTENSION_JAR;

  static {
    try (Stream<Path> paths = Files.walk(Paths.get("target"))) {
      CONNECT_EXTENSION_JAR = paths
        .filter(path -> path.getFileName().toString().endsWith("-all.jar"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find connect extension JAR"))
        .toFile();

    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public ConfluentKafkaConnectContainer(String imageName) {
    super(new ImageFromDockerfile()
      .withFileFromFile("mredjem-kafka-connect-secret-registry-extension.jar", CONNECT_EXTENSION_JAR)
      .withDockerfileFromBuilder(builder -> builder
        .from(imageName)
        .copy("mredjem-kafka-connect-secret-registry-extension.jar", PLUGIN_PATH)
        .build()
      ));
  }
}
