package com.github.mredjem.kafka.connect.oidc.ccloud;

import dev.cel.common.types.CelType;
import dev.cel.common.types.SimpleType;

import java.util.HashMap;
import java.util.Map;

public final class CelFilterVariables {

  static final Map<String, CelType> VARIABLES = new HashMap<>();

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

  private CelFilterVariables() {}
}
