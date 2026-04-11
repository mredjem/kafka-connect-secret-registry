package com.github.mredjem.kafka.connect.oidc.utils;

import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;

import java.util.HashMap;
import java.util.Map;

public final class CelUtils {

  private static final CelCompiler CEL_COMPILER = CelCompilerFactory.standardCelCompilerBuilder()
    .addVar("claims.auth_time", SimpleType.INT)
    .addVar("claims.aud", SimpleType.STRING)
    .addVar("claims.cid", SimpleType.STRING)
    .addVar("claims.exp", SimpleType.INT)
    .addVar("claims.iat", SimpleType.INT)
    .addVar("claims.iss", SimpleType.STRING)
    .addVar("claims.jti", SimpleType.STRING)
    .addVar("claims.sub", SimpleType.STRING)
    .addVar("claims.scp", SimpleType.ANY)
    .addVar("claims.uid", SimpleType.STRING)
    .addVar("claims.ver", SimpleType.INT)
    .addVar("claims.acr", SimpleType.STRING)
    // azure specific
    .addVar("claims.azp", SimpleType.STRING)
    .addVar("claims.appid", SimpleType.STRING)
    .setResultType(SimpleType.BOOL)
    .build();

  private static final CelRuntime CEL_RUNTIME = CelRuntimeFactory.standardCelRuntimeBuilder().build();

  private CelUtils() {}

  public static boolean evaluateFilter(Map<String, Object> claims, String celFilter) {
    try {
      Map<String, Object> variables = getVariables(claims);

      CelAbstractSyntaxTree ast = CEL_COMPILER.compile(celFilter).getAst();

      CelRuntime.Program program = CEL_RUNTIME.createProgram(ast);

      return (boolean) program.eval(variables);

    } catch (final CelValidationException | CelEvaluationException ignored) {
      return false;
    }
  }

  private static Map<String, Object> getVariables(Map<String, Object> claims) {
    Map<String, Object> variables = new HashMap<>();

    claims.forEach((name, value) -> variables.put("claims." + name, value));

    return variables;
  }
}
