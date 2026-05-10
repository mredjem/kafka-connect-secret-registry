package com.github.mredjem.kafka.connect.extensions.dtos;

import com.github.mredjem.kafka.connect.Secret;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecretDto {

  private String path;
  private String key;
  private int version;
  private String secret;

  private SecretDto(Secret secret) {
    this.path = secret.getVersion().getKey().getPath().getValue();
    this.key = secret.getVersion().getKey().getValue();
    this.version = secret.getVersion().getValue();
    this.secret = secret.getValue();
  }

  public static SecretDto toDto(Secret secret) {
    return new SecretDto(secret);
  }
}
