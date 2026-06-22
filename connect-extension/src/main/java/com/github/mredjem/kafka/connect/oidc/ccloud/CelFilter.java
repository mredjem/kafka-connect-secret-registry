package com.github.mredjem.kafka.connect.oidc.ccloud;

import org.projectnessie.cel.tools.Script;
import org.projectnessie.cel.tools.ScriptCreateException;
import org.projectnessie.cel.tools.ScriptException;
import org.projectnessie.cel.tools.ScriptHost;

import java.util.HashMap;
import java.util.Map;

import static com.github.mredjem.kafka.connect.oidc.ccloud.CelFilterVariables.VARIABLES;
import static com.github.mredjem.kafka.connect.oidc.ccloud.CelFilterVariables.VARIABLE_NAMES;

public class CelFilter {

  private static final ScriptHost SCRIPT_HOST = ScriptHost.newBuilder().build();

  private final Script script;

  private CelFilter(String celFilter) {
    try {
      this.script = SCRIPT_HOST.buildScript(celFilter)
        .withDeclarations(VARIABLES)
        .build();

    } catch (final ScriptCreateException e) {
      throw new IllegalArgumentException("Invalid CEL filter: " + celFilter, e);
    }
  }

  public static CelFilter parse(String celFilter) {
    return new CelFilter(celFilter);
  }

  public boolean evaluate(Map<String, Object> variables) {
    try {
      Map<String, Object> arguments = this.filterVariables(variables);

      return this.script.execute(Boolean.class, arguments);

    } catch (final ScriptException ignored) {
      return false;
    }
  }

  private Map<String, Object> filterVariables(Map<String, Object> variables) {
    Map<String, Object> filtered = new HashMap<>();

    variables.forEach((name, value) -> {
      String variableName = "claims." + name;

      if (VARIABLE_NAMES.contains(variableName)) {
        filtered.put(variableName, value);
      }
    });

    return filtered;
  }
}
