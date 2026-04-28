package com.github.mredjem.kafka.connect.oidc.ccloud;

import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerBuilder;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;

import java.util.HashMap;
import java.util.Map;

import static com.github.mredjem.kafka.connect.oidc.ccloud.CelFilterVariables.VARIABLES;

public class CelFilter {

  private static final CelCompiler CEL_COMPILER;

  static {
    CelCompilerBuilder celCompilerBuilder = CelCompilerFactory.standardCelCompilerBuilder();

    VARIABLES.forEach(celCompilerBuilder::addVar);

    CEL_COMPILER = celCompilerBuilder
      .setResultType(SimpleType.BOOL)
      .build();
  }

  private static final CelRuntime CEL_RUNTIME = CelRuntimeFactory.standardCelRuntimeBuilder().build();

  private final CelRuntime.Program program;

  private CelFilter(String celFilter) {
    try {
      CelAbstractSyntaxTree ast = CEL_COMPILER.compile(celFilter).getAst();

      this.program = CEL_RUNTIME.createProgram(ast);

    } catch (final CelValidationException | CelEvaluationException e) {
      throw new IllegalArgumentException("Invalid CEL filter: " + celFilter, e);
    }
  }

  public static CelFilter parse(String celFilter) {
    return new CelFilter(celFilter);
  }

  public boolean evaluate(Map<String, Object> variables) {
    try {
      Map<String, Object> filtered = this.filterVariables(variables);

      return (boolean) this.program.eval(filtered);

    } catch (final CelEvaluationException ignored) {
      return false;
    }
  }

  private Map<String, Object> filterVariables(Map<String, Object> variables) {
    Map<String, Object> filtered = new HashMap<>();

    variables.forEach((name, value) -> {
      String variableName = "claims." + name;

      if (VARIABLES.containsKey(variableName)) {
        filtered.put(variableName, value);
      }
    });

    return filtered;
  }
}
