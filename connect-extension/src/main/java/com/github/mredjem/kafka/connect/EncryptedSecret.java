package com.github.mredjem.kafka.connect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class EncryptedSecret {

  private final byte[] secret;
  private final byte[] salt;
  private final byte[] iv;
}
