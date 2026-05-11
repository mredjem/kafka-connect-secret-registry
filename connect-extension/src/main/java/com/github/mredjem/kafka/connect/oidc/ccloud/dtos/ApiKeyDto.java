package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiKeyDto {

  private String id;
  private SpecDto spec;
}
