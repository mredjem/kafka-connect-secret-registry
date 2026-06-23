package com.github.mredjem.kafka.connect.cel;

import com.github.mredjem.kafka.connect.cel.exceptions.ParseException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Lexer {

  private final String sequence;

  private int pos;

  public List<Token> tokenize() throws ParseException {
    List<Token> tokens = new ArrayList<>();

    while (this.pos < this.sequence.length()) {
      char c = this.sequence.charAt(this.pos);

      if (Character.isWhitespace(c)) {
        this.pos++;
        continue;
      }

      if (Character.isLetter(c)) {
        tokens.add(readIdentifier());
        continue;
      }

      if (Character.isDigit(c)) {
        tokens.add(readNumber());
        continue;
      }

      if (c == '\'') {
        tokens.add(readString());
        this.pos += 2;
        continue;
      }

      switch (c) {
        case '.' -> {
          tokens.add(Token.of(TokenType.DOT, "."));
          this.pos++;
        }
        case '&' -> {
          if (peek('&')) {
            tokens.add(Token.of(TokenType.AND, "&&"));
            this.pos += 2;
          }
        }
        case '|' -> {
          if (peek('|')) {
            tokens.add(Token.of(TokenType.OR, "||"));
            this.pos += 2;
          }
        }
        case '=' -> {
          if (peek('=')) {
            tokens.add(Token.of(TokenType.EQ, "=="));
            this.pos += 2;
          }
        }
        case '!' -> {
          if (peek('=')) {
            tokens.add(Token.of(TokenType.NE, "!="));
            this.pos += 2;
          }
        }
        case '<' -> {
          if (peek('=')) {
            tokens.add(Token.of(TokenType.LTE, "<="));
            this.pos += 2;
          } else {
            tokens.add(Token.of(TokenType.LT, "<"));
            this.pos++;
          }
        }
        case '>' -> {
          if (peek('=')) {
            tokens.add(Token.of(TokenType.GTE, ">="));
            this.pos += 2;
          } else {
            tokens.add(Token.of(TokenType.GT, ">"));
            this.pos++;
          }
        }
        case '(' -> {
          tokens.add(Token.of(TokenType.LPAREN, "("));
          this.pos++;
        }
        case ')' -> {
          tokens.add(Token.of(TokenType.RPAREN, ")"));
          this.pos++;
        }
        default -> throw new ParseException("Unexpected character '" + c + "' at position " + this.pos);
      }
    }

    return tokens;
  }

  private Token readIdentifier() {
    int start = this.pos;

    while (this.pos < this.sequence.length() && Character.isLetter(this.sequence.charAt(this.pos))) {
      this.pos++;
    }

    String text = this.sequence.substring(start, this.pos);

    if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
      return Token.of(TokenType.BOOLEAN, text.toLowerCase());
    }

    return Token.of(TokenType.IDENTIFIER, text);
  }

  private Token readNumber() {
    int start = this.pos;

    while (this.pos < this.sequence.length() && Character.isDigit(this.sequence.charAt(this.pos))) {
      this.pos++;
    }

    return Token.of(TokenType.NUMBER, this.sequence.substring(start, this.pos));
  }

  private Token readString() {
    int start = this.pos;

    while (this.pos < this.sequence.length() && !peek('\'')) {
      this.pos++;
    }

    return Token.of(TokenType.STRING, this.sequence.substring(start + 1, this.pos + 1));
  }

  private boolean peek(char expected) {
    return this.pos + 1 < this.sequence.length() && this.sequence.charAt(this.pos + 1) == expected;
  }
}
