package com.github.mredjem.kafka.connect.oidc.ccloud;

import com.github.mredjem.kafka.connect.cel.Evaluator;
import com.github.mredjem.kafka.connect.cel.Parser;
import com.github.mredjem.kafka.connect.cel.expressions.Expr;

import java.util.Map;

public class CelFilter {

  private final Expr ast;

  private CelFilter(String celFilter) {
    try {
      this.ast = new Parser(celFilter).parse();

    } catch (final Exception e) {
      throw new IllegalArgumentException("Invalid CEL filter: " + celFilter, e);
    }
  }

  public static CelFilter parse(String celFilter) {
    return new CelFilter(celFilter);
  }

  public boolean evaluate(Map<String, Object> args) {
    try {
      return (boolean) new Evaluator().evaluate(this.ast, args);

    } catch (final Exception ignored) {
      return false;
    }
  }
}
