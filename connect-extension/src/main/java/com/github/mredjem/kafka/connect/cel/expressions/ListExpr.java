package com.github.mredjem.kafka.connect.cel.expressions;

import java.util.List;

public record ListExpr(List<Expr> values) implements Expr {
}
