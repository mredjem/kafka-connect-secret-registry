package com.github.mredjem.kafka.connect.cel;

import com.github.mredjem.kafka.connect.cel.exceptions.EvaluationException;
import com.github.mredjem.kafka.connect.cel.expressions.BinaryExpr;
import com.github.mredjem.kafka.connect.cel.expressions.Expr;
import com.github.mredjem.kafka.connect.cel.expressions.FieldAccessExpr;
import com.github.mredjem.kafka.connect.cel.expressions.ListExpr;
import com.github.mredjem.kafka.connect.cel.expressions.LiteralExpr;
import com.github.mredjem.kafka.connect.cel.expressions.VariableExpr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Evaluator {

  public Object evaluate(Expr expr, Map<String, Object> args) throws EvaluationException {
    if (expr instanceof LiteralExpr literalExpr) {
      return literalExpr.value();
    }

    if (expr instanceof ListExpr listExpr) {
      List<Object> values = new ArrayList<>();

      for (Expr valueExpr : listExpr.values()) {
        Object value = evaluate(valueExpr, args);

        values.add(value);
      }

      return values;
    }

    if (expr instanceof VariableExpr variableExpr) {
      return args.get(variableExpr.name());
    }

    if (expr instanceof FieldAccessExpr fieldAccessExpr) {
      Object target = evaluate(fieldAccessExpr.target(), args);

      return resolveField(target, fieldAccessExpr.field());
    }

    BinaryExpr binaryExpr = (BinaryExpr) expr;

    Object left = evaluate(binaryExpr.left(), args);
    Object right = evaluate(binaryExpr.right(), args);

    return switch (binaryExpr.op()) {
      case "==" -> left.equals(right);
      case "!=" -> !left.equals(right);
      case "<" -> Double.compare(asDouble(left), asDouble(right)) < 0;
      case ">" -> Double.compare(asDouble(left), asDouble(right)) > 0;
      case "<=" -> Double.compare(asDouble(left), asDouble(right)) <= 0;
      case ">=" -> Double.compare(asDouble(left), asDouble(right)) >= 0;
      case "&&" -> Boolean.logicalAnd(asBoolean(left), asBoolean(right));
      case "||" -> Boolean.logicalOr(asBoolean(left), asBoolean(right));
      case "in" -> asCollection(right).contains(left);
      default -> throw new EvaluationException("Unexpected operator '" + binaryExpr.op() + "'");
    };
  }

  private double asDouble(Object value) throws EvaluationException {
    if (!(value instanceof Number n)) {
      throw new EvaluationException("Expected numeric value but got: " + value);
    }

    return n.doubleValue();
  }

  private boolean asBoolean(Object value) throws EvaluationException {
    if (!(value instanceof Boolean b)) {
      throw new EvaluationException("Expected boolean value but got: " + value);
    }

    return b;
  }

  private Collection<?> asCollection(Object value) throws EvaluationException {
    if (!(value instanceof Collection<?> c)) {
      throw new EvaluationException("Expected collection value but got: " + value);
    }

    return c;
  }

  private Object resolveField(Object target, String field) throws EvaluationException {
    if (target == null) {
      throw new EvaluationException("Cannot resolve field '" + field + "' on null");
    }

    if (target instanceof Map<?, ?> map) {
      return map.get(field);
    }

    throw new EvaluationException("Field access unsupported on " + target.getClass());
  }
}
