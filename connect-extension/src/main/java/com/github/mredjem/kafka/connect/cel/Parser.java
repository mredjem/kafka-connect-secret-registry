package com.github.mredjem.kafka.connect.cel;

import com.github.mredjem.kafka.connect.cel.exceptions.ParseException;
import com.github.mredjem.kafka.connect.cel.expressions.BinaryExpr;
import com.github.mredjem.kafka.connect.cel.expressions.Expr;
import com.github.mredjem.kafka.connect.cel.expressions.FieldAccessExpr;
import com.github.mredjem.kafka.connect.cel.expressions.LiteralExpr;
import com.github.mredjem.kafka.connect.cel.expressions.VariableExpr;

import java.util.List;

public class Parser {

  private final List<Token> tokens;

  private int pos;

  public Parser(String sequence) throws ParseException {
    this.tokens = new Lexer(sequence).tokenize();
  }

  public Expr parse() throws ParseException {
    return this.parseOr();
  }

  private Expr parseOr() throws ParseException {
    Expr expr = parseAnd();

    while (match(TokenType.OR)) {
      expr = new BinaryExpr(expr, "||", parseAnd());
    }

    return expr;
  }

  private Expr parseAnd() throws ParseException {
    Expr expr = parseComparison();

    while (match(TokenType.AND)) {
      expr = new BinaryExpr(expr, "&&", parseComparison());
    }

    return expr;
  }

  private Expr parseComparison() throws ParseException {
    Expr left = parsePrimary();

    while (true) {
      if (match(TokenType.EQ)) {
        left = new BinaryExpr(left, "==", parsePrimary());
      } else if (match(TokenType.NE)) {
        left = new BinaryExpr(left, "!=", parsePrimary());
      } else if (match(TokenType.GT)) {
        left = new BinaryExpr(left, ">", parsePrimary());
      } else if (match(TokenType.LT)) {
        left = new BinaryExpr(left, "<", parsePrimary());
      } else if (match(TokenType.GTE)) {
        left = new BinaryExpr(left, ">=", parsePrimary());
      } else if (match(TokenType.LTE)) {
        left = new BinaryExpr(left, "<=", parsePrimary());
      } else {
        break;
      }
    }

    return left;
  }

  private Expr parsePrimary() throws ParseException {
    Token token = nextToken();

    return switch (token.type()) {
      case IDENTIFIER -> {
        Expr expr = new VariableExpr(token.value());

        while (match(TokenType.DOT)) {
          Token field = take(TokenType.IDENTIFIER);

          expr = new FieldAccessExpr(expr, field.value());
        }

        yield expr;
      }
      case STRING -> new LiteralExpr(token.value());
      case NUMBER -> new LiteralExpr(Double.parseDouble(token.value()));
      case BOOLEAN -> new LiteralExpr(Boolean.parseBoolean(token.value()));
      case LPAREN -> {
        Expr expr = parse();
        take(TokenType.RPAREN);
        yield expr;
      }
      default -> throw new ParseException("Unexpected token '" + token.value() + "' at position " + this.pos);
    };
  }

  private Token nextToken() {
    return this.tokens.get(this.pos++);
  }

  private boolean match(TokenType type) {
    if (check(type)) {
      this.pos++;
      return true;
    }

    return false;
  }

  private Token take(TokenType type) throws ParseException {
    if (!check(type)) {
      throw new ParseException("Unexpected type '" + type + "' at position " + this.pos);
    }

    return nextToken();
  }

  private boolean check(TokenType type) {
    return this.pos < this.tokens.size() && peek().type() == type;
  }

  private Token peek() {
    return this.tokens.get(this.pos);
  }
}
