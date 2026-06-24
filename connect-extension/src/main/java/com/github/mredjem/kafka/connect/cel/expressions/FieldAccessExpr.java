package com.github.mredjem.kafka.connect.cel.expressions;

public record FieldAccessExpr(Expr target, String field) implements Expr {
}
