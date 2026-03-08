package com.github.mredjem.kafka.connect.internals.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class SharedObjectMapper {

  public static final SharedObjectMapper INSTANCE = new SharedObjectMapper();

  private final ObjectMapper objectMapper;

  private SharedObjectMapper() {
    this.objectMapper = new ObjectMapper();
  }

  public static SharedObjectMapper getInstance() {
    return INSTANCE;
  }

  public byte[] serialize(Object obj) throws JsonProcessingException {
    return this.objectMapper.writeValueAsBytes(obj);
  }

  public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
    return this.objectMapper.readValue(bytes, clazz);
  }
}
