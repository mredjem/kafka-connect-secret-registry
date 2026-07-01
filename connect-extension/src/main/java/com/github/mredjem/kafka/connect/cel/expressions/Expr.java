package com.github.mredjem.kafka.connect.cel.expressions;

public sealed interface Expr permits ListExpr, BinaryExpr, FieldAccessExpr, LiteralExpr, VariableExpr {
}
