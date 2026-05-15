package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataDto {

  private String first;
  private String last;
  private String prev;
  private String next;
}
