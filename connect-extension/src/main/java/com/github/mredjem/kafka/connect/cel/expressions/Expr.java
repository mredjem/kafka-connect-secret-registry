package com.github.mredjem.kafka.connect.cel.expressions;

public sealed interface Expr permits LiteralExpr, VariableExpr, BinaryExpr, FieldAccessExpr {
}
