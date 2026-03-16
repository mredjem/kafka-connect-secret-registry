package com.github.mredjem.kafka.connect.extensions.dtos;

import com.github.mredjem.kafka.connect.Secret;

public class SecretDto {

  private String path;

  private String key;

  private int version;

  private String secret;

  private SecretDto(Secret secret) {
    this.path = secret.getVersion().getKey().getPath().getPath();
    this.key = secret.getVersion().getKey().getKey();
    this.version = secret.getVersion().getVersion();
    this.secret = secret.getSecret();
  }

  public SecretDto() {
  }

  public static SecretDto toDto(Secret secret) {
    return new SecretDto(secret);
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getVersion() {
    return this.version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getSecret() {
    return this.secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }
}
