package com.github.mredjem.kafka.connect.oidc.ccloud;

import com.google.api.expr.v1alpha1.Decl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.projectnessie.cel.checker.Decls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CelFilterVariables {

  static final List<Decl> VARIABLES = new ArrayList<>();

  static {
    VARIABLES.add(Decls.newVar("claims.auth_time", Decls.Int));
    VARIABLES.add(Decls.newVar("claims.aud", Decls.String));
    VARIABLES.add(Decls.newVar("claims.cid", Decls.String));
    VARIABLES.add(Decls.newVar("claims.exp", Decls.Int));
    VARIABLES.add(Decls.newVar("claims.iat", Decls.Int));
    VARIABLES.add(Decls.newVar("claims.iss", Decls.String));
    VARIABLES.add(Decls.newVar("claims.jti", Decls.String));
    VARIABLES.add(Decls.newVar("claims.sub", Decls.String));
    VARIABLES.add(Decls.newVar("claims.scp", Decls.Any));
    VARIABLES.add(Decls.newVar("claims.uid", Decls.String));
    VARIABLES.add(Decls.newVar("claims.ver", Decls.Int));
    VARIABLES.add(Decls.newVar("claims.acr", Decls.String));
    // azure specific
    VARIABLES.add(Decls.newVar("claims.azp", Decls.String));
    VARIABLES.add(Decls.newVar("claims.appid", Decls.String));
  }

  static final Set<String> VARIABLE_NAMES = new HashSet<>();

  static {
    VARIABLES.forEach(decl -> VARIABLE_NAMES.add(decl.getName()));
  }
}
