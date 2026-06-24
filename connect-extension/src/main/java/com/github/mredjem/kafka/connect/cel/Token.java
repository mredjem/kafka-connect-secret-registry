package com.github.mredjem.kafka.connect.cel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "of")
public class Token {

  private final TokenType type;
  private final String value;
}
