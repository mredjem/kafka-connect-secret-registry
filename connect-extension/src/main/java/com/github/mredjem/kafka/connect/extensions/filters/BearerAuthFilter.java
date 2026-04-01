package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.Operation;
import com.github.mredjem.kafka.connect.extensions.utils.FilterUtils;
import com.github.mredjem.kafka.connect.extensions.utils.RBACUtils;
import com.github.mredjem.kafka.connect.internals.KafkaAuthorizationRepository;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

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
    Map<String, String> kafkaConfigs = ConfigUtils.getConfigsForPrefix("kafkastore.", configs);

    this.authorizationPort = KafkaAuthorizationRepository.create(kafkaConfigs);
  }

  public static BearerAuthFilter create(Map<String, String> configs) {
    return new BearerAuthFilter(configs);
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

    if (!this.authorizationPort.validateToken(authenticationCredentials)) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User token is not valid"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    Operation operation = RBACUtils.getOperationForRequest(containerRequestContext);

    boolean operationAllowed = this.authorizationPort.getAppRoles(authenticationCredentials)
      .stream()
      .anyMatch(role -> role.allows(operation));

    if (!operationAllowed) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User token is not allowed to access resource"));

      containerRequestContext.abortWith(errorResponse);
    }
  }
}
