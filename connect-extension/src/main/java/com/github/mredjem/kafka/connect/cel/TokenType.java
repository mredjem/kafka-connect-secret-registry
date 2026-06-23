package com.github.mredjem.kafka.connect.cel;

public enum TokenType {
  IDENTIFIER,
  DOT,

  STRING,
  NUMBER,
  BOOLEAN,

  AND,
  OR,

  EQ,
  NE,
  LT,
  LTE,
  GT,
  GTE,

  LPAREN,
  RPAREN,
}
