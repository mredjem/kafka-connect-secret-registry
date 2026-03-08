package com.github.mredjem.kafka.connect.extensions.dtos;

public class ErrorDto {

  private long timestamp;

  private String path;

  private int code;

  private String reason;

  private String exception;

  private String message;

  public long getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getCode() {
    return this.code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getReason() {
    return this.reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getException() {
    return this.exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
