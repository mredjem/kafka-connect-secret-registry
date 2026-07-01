package com.github.mredjem.kafka.connect.cel;

public enum TokenType {
  IDENTIFIER,
  DOT,
  COMMA,

  STRING,
  NUMBER,
  BOOLEAN,

  AND,
  OR,
  IN,

  EQ,
  NE,
  LT,
  LTE,
  GT,
  GTE,

  LPAREN,
  RPAREN,
  LBRACKET,
  RBRACKET,
}
