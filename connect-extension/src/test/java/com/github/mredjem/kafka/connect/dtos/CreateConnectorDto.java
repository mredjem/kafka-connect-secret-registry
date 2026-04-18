package com.github.mredjem.kafka.connect.dtos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CreateConnectorDto {

  private final Map<String, String> config;

  private CreateConnectorDto(Map<String, String> config) {
    this.config = Collections.unmodifiableMap(config);
  }

  public static CreateConnectorDto createDummy() {
    Map<String, String> connectorConfig = new HashMap<>();

    connectorConfig.put("connector.class", "org.apache.kafka.connect.mirror.MirrorSourceConnector");
    connectorConfig.put("tasks.max", "${secret:prd-connector:tasks.max}");
    connectorConfig.put("topics", "_connect-secrets");
    connectorConfig.put("source.cluster.alias", "source");
    connectorConfig.put("source.cluster.bootstrap.servers", "kafka:29092");
    connectorConfig.put("target.cluster.bootstrap.servers", "kafka:29092");

    return new CreateConnectorDto(connectorConfig);
  }

  public String getName() {
    return "prd-connector";
  }

  public Map<String, String> getConfig() {
    return this.config;
  }
}
