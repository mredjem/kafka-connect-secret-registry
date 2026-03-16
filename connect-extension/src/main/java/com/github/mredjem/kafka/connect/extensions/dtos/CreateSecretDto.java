package com.github.mredjem.kafka.connect.extensions.dtos;

public class CreateSecretDto {

  private String secret;

  public CreateSecretDto() {
  }

  public static CreateSecretDto of(String secret) {
    CreateSecretDto createSecretDto = new CreateSecretDto();

    createSecretDto.setSecret(secret);

    return createSecretDto;
  }

  public String getSecret() {
    return this.secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }
}
