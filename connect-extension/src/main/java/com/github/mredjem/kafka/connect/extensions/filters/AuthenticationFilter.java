package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.extensions.rbac.RbacRules;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static com.github.mredjem.kafka.connect.extensions.api.ApiExceptionHandler.toErrorResponse;

public class AuthenticationFilter implements ContainerRequestFilter {

  private final ContainerRequestFilter adminAuthenticationFilter;

  private final ContainerRequestFilter rbacAuthenticationFilter;

  private AuthenticationFilter(Map<String, String> configs, AuthorizationPort authorizationPort) {
    this.adminAuthenticationFilter = AdminAuthenticationFilter.create(configs);
    this.rbacAuthenticationFilter = RbacAuthenticationFilter.create(authorizationPort);
  }

  public static AuthenticationFilter create(Map<String, String> configs, AuthorizationPort authorizationPort) {
    return new AuthenticationFilter(configs, authorizationPort);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    if (RbacRules.isInternalRequest(containerRequestContext) || RbacRules.isAllowedAnonymously(containerRequestContext)) {
      return;
    }

    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION));

    if (AuthenticationKind.NONE == authenticationCredentials.getKind()) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new NotAuthorizedException("Authorization header is not valid", "Basic|Bearer"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    if (((AdminAuthenticationFilter) this.adminAuthenticationFilter).applicableTo(containerRequestContext)) {
      this.adminAuthenticationFilter.filter(containerRequestContext);

      return;
    }

    this.rbacAuthenticationFilter.filter(containerRequestContext);
  }
}
