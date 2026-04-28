package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.extensions.rbac.RbacRules;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.github.mredjem.kafka.connect.extensions.api.ApiExceptionHandler.toErrorResponse;

public class AuthenticationFilter implements ContainerRequestFilter {

  private final ContainerRequestFilter nextFilter;

  private AuthenticationFilter(ContainerRequestFilter nextFilter) {
    this.nextFilter = nextFilter;
  }

  public static AuthenticationFilter create(ContainerRequestFilter nextFilter) {
    return new AuthenticationFilter(nextFilter);
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

    this.nextFilter.filter(containerRequestContext);
  }
}
