package com.github.mredjem.kafka.connect.cel.expressions;

public record BinaryExpr(Expr left, String op, Expr right) implements Expr {
}
