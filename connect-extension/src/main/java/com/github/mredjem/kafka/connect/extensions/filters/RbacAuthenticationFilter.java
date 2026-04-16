package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.extensions.rbac.RbacRules;
import com.github.mredjem.kafka.connect.extensions.rbac.RequestedAction;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.github.mredjem.kafka.connect.extensions.api.ApiExceptionHandler.toErrorResponse;

public class RbacAuthenticationFilter implements ContainerRequestFilter {

  private final AuthorizationPort authorizationPort;

  private RbacAuthenticationFilter(AuthorizationPort authorizationPort) {
    this.authorizationPort = authorizationPort;
  }

  public static RbacAuthenticationFilter create(AuthorizationPort authorizationPort) {
    return new RbacAuthenticationFilter(authorizationPort);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION));

    RequestedAction requestedAction = RbacRules.getActionForRequest(containerRequestContext);

    if (requestedAction == null) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("Resource being accessed is unregistered"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    boolean hasAccess = this.authorizationPort.checkAccess(authenticationCredentials, requestedAction.getOperation(), requestedAction.getResourceName());

    if (!hasAccess) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User is not allowed to access resource"));

      containerRequestContext.abortWith(errorResponse);
    }
  }
}
