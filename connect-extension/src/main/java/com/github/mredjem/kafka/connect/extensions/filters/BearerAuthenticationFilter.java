package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.extensions.rbac.RequestedAction;
import com.github.mredjem.kafka.connect.extensions.rbac.RbacRules;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static com.github.mredjem.kafka.connect.extensions.api.ApiExceptionHandler.toErrorResponse;

public class BearerAuthenticationFilter implements ContainerRequestFilter {

  private final AuthorizationPort authorizationPort;

  private BearerAuthenticationFilter(AuthorizationPort authorizationPort) {
    this.authorizationPort = authorizationPort;
  }

  public static BearerAuthenticationFilter create(AuthorizationPort authorizationPort) {
    return new BearerAuthenticationFilter(authorizationPort);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) {
    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION));

    RequestedAction requestedAction = RbacRules.getActionForRequest(containerRequestContext);

    if (requestedAction == null) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("Resource being accessed is unregistered"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    boolean checkAccess = this.authorizationPort.checkAccess(authenticationCredentials, requestedAction.getOperation(), requestedAction.getResourceName());

    if (!checkAccess) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User token is not allowed to access resource"));

      containerRequestContext.abortWith(errorResponse);
    }
  }
}
