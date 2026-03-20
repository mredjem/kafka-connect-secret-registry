package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.internals.KafkaClusterPingRepository;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApiExceptionHandler.toErrorResponse;

public class BearerAuthFilter implements ContainerRequestFilter {

  private final AuthorizationPort authorizationPort;

  private BearerAuthFilter(Map<String, String> configs) {
    this.authorizationPort = KafkaClusterPingRepository.create(configs);
  }

  public static BearerAuthFilter create(Map<String, String> configs) {
    return new BearerAuthFilter(configs);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) {
    if (FilterUtils.isAllowedAnonymously(containerRequestContext)) {
      return;
    }

    String bearerCredentials = FilterUtils.getBearerCredentials(containerRequestContext);

    if (bearerCredentials.isEmpty()) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new NotAuthorizedException("Authorization header is not valid"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(AuthenticationKind.BEARER, bearerCredentials);

    if (!this.authorizationPort.checkAccess(authenticationCredentials)) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("Access is denied, check your configuration"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    if (FilterUtils.isWriteAccess(containerRequestContext)) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User is allowed read access only"));

      containerRequestContext.abortWith(errorResponse);
    }
  }
}
