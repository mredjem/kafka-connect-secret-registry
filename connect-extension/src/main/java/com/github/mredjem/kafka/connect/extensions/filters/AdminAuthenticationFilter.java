package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.ScopedCredentials;
import com.github.mredjem.kafka.connect.extensions.rbac.RbacRules;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.mredjem.kafka.connect.extensions.api.ApiExceptionHandler.toErrorResponse;

public class AdminAuthenticationFilter implements ContainerRequestFilter {

  private static final String SUPER_ADMINS_CONFIG = "super.admins";

  private final List<ScopedCredentials> superAdmins;

  private AdminAuthenticationFilter(Map<String, String> configs) {
    this.superAdmins = Arrays.stream(ConfigUtils.getOrThrow(SUPER_ADMINS_CONFIG, configs).split(","))
      .map(ScopedCredentials::of)
      .collect(Collectors.toList());
  }

  public static AdminAuthenticationFilter create(Map<String, String> configs) {
    return new AdminAuthenticationFilter(configs);
  }

  public boolean applicableTo(ContainerRequestContext containerRequestContext) {
    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION));

    return AuthenticationKind.BASIC == authenticationCredentials.getKind() && this.findSuperAdmin(authenticationCredentials.getCredentials()) != null;
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION));

    ScopedCredentials superAdmin = this.findSuperAdmin(authenticationCredentials.getCredentials());

    if (superAdmin.hasReadScope() && RbacRules.isWriteAccess(containerRequestContext)) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User is allowed read access only"));

      containerRequestContext.abortWith(errorResponse);
    }
  }

  private ScopedCredentials findSuperAdmin(String basicCredentials) {
    return this.superAdmins.stream()
      .filter(superAdmin -> superAdmin.getCredentials().equals(basicCredentials))
      .findFirst()
      .orElse(null);
  }
}
