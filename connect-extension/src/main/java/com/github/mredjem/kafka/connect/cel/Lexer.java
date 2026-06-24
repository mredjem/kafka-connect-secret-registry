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
      } else if (Character.isLetter(c)) {
        tokens.add(readIdentifier());
      } else if (Character.isDigit(c)) {
        tokens.add(readNumber());
      } else if (c == '\'') {
        tokens.add(readString());
        this.pos += 2;
      } else {
        tokens.add(readOperator());
      }
    }

    return tokens;
  }

  private Token readOperator() throws ParseException {
    char c = this.sequence.charAt(this.pos);

    return switch (c) {
      case '.' -> {
        this.pos++;
        yield Token.of(TokenType.DOT, ".");
      }
      case '&' -> {
        if (peek('&')) {
          this.pos += 2;
          yield Token.of(TokenType.AND, "&");
        }
        throw new ParseException("Unexpected character '" + c + "' at position " + this.pos);
      }
      case '|' -> {
        if (peek('|')) {
          this.pos += 2;
          yield Token.of(TokenType.OR, "|");
        }
        throw new ParseException("Unexpected character '" + c + "' at position " + this.pos);
      }
      case '=' -> {
        if (peek('=')) {
          this.pos += 2;
          yield Token.of(TokenType.EQ, "==");
        }
        throw new ParseException("Unexpected character '" + c + "' at position " + this.pos);
      }
      case '!' -> {
        if (peek('=')) {
          this.pos += 2;
          yield Token.of(TokenType.NE, "!=");
        }
        throw new ParseException("Unexpected character '" + c + "' at position " + this.pos);
      }
      case '<' -> {
        if (peek('=')) {
          this.pos += 2;
          yield Token.of(TokenType.LTE, "<=");
        } else {
          this.pos++;
          yield Token.of(TokenType.LT, "<");
        }
      }
      case '>' -> {
        if (peek('=')) {
          this.pos += 2;
          yield Token.of(TokenType.GTE, ">=");
        } else {
          this.pos++;
          yield Token.of(TokenType.GT, ">");
        }
      }
      case '(' -> {
        this.pos++;
        yield Token.of(TokenType.LPAREN, "(");
      }
      case ')' -> {
        this.pos++;
        yield Token.of(TokenType.RPAREN, ")");
      }
      default -> throw new ParseException("Unexpected character '" + c + "' at position " + this.pos);
    };
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
