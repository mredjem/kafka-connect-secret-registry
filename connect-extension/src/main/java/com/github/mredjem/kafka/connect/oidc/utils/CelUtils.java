package com.github.mredjem.kafka.connect.oidc.utils;

import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.CelType;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerBuilder;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;

import java.util.HashMap;
import java.util.Map;

public final class CelUtils {

  private static final Map<String, CelType> VARIABLES = new HashMap<>();

  static {
    VARIABLES.put("claims.auth_time", SimpleType.INT);
    VARIABLES.put("claims.aud", SimpleType.STRING);
    VARIABLES.put("claims.cid", SimpleType.STRING);
    VARIABLES.put("claims.exp", SimpleType.INT);
    VARIABLES.put("claims.iat", SimpleType.INT);
    VARIABLES.put("claims.iss", SimpleType.STRING);
    VARIABLES.put("claims.jti", SimpleType.STRING);
    VARIABLES.put("claims.sub", SimpleType.STRING);
    VARIABLES.put("claims.scp", SimpleType.ANY);
    VARIABLES.put("claims.uid", SimpleType.STRING);
    VARIABLES.put("claims.ver", SimpleType.INT);
    VARIABLES.put("claims.acr", SimpleType.STRING);
    // azure specific
    VARIABLES.put("claims.azp", SimpleType.STRING);
    VARIABLES.put("claims.appid", SimpleType.STRING);
  }

  private static final CelCompiler CEL_COMPILER;

  static {
    CelCompilerBuilder celCompilerBuilder = CelCompilerFactory.standardCelCompilerBuilder();

    VARIABLES.forEach(celCompilerBuilder::addVar);

    CEL_COMPILER = celCompilerBuilder
      .setResultType(SimpleType.BOOL)
      .build();
  }

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

    claims.forEach((name, value) -> {
      String variableName = "claims." + name;

      if (VARIABLES.containsKey(variableName)) {
        variables.put(variableName, value);
      }
    });

    return variables;
  }
}
