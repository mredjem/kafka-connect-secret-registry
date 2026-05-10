package com.github.mredjem.kafka.connect.extensions.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDto {

  private long timestamp;
  private String path;
  private int code;
  private String reason;
  private String exception;
  private String message;
}
