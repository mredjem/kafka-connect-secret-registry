package com.github.mredjem.kafka.connect.extensions.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSecretDto {

  private String secret;

  public CreateSecretDto() {
    // empty constructor for jackson
  }

  public static CreateSecretDto of(String secret) {
    CreateSecretDto createSecretDto = new CreateSecretDto();

    createSecretDto.setSecret(secret);

    return createSecretDto;
  }
}
