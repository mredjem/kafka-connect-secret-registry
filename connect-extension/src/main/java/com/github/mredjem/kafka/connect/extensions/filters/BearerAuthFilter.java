package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.extensions.utils.FilterUtils;
import com.github.mredjem.kafka.connect.extensions.utils.RBACUtils;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import static com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApiExceptionHandler.toErrorResponse;

public class BearerAuthFilter implements ContainerRequestFilter {

  private final AuthorizationPort authorizationPort;

  private BearerAuthFilter(AuthorizationPort authorizationPort) {
    this.authorizationPort = authorizationPort;
  }

  public static BearerAuthFilter create(AuthorizationPort authorizationPort) {
    return new BearerAuthFilter(authorizationPort);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) {
    String bearerCredentials = FilterUtils.getBearerCredentials(containerRequestContext);

    if (bearerCredentials.isEmpty()) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new NotAuthorizedException("Authorization header is not valid", "Basic|Bearer"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(AuthenticationKind.BEARER, bearerCredentials);

    Operation operation = RBACUtils.getOperationForRequest(containerRequestContext);

    String resourceName = RBACUtils.getResourceForRequest(containerRequestContext, operation);

    if (!this.authorizationPort.checkAccess(authenticationCredentials, operation, resourceName)) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User token is not allowed to access resource"));

      containerRequestContext.abortWith(errorResponse);
    }
  }
}
