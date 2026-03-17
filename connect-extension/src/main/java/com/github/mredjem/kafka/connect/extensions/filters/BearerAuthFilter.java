package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.AuthorizationPort;
import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.extensions.exceptions.ForbiddenException;
import com.github.mredjem.kafka.connect.extensions.exceptions.UnauthorizedException;
import com.github.mredjem.kafka.connect.internals.KafkaClusterPingRepository;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.Map;

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
      throw new UnauthorizedException("Authorization header is not valid");
    }

    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(AuthenticationKind.BEARER, bearerCredentials);

    if (!this.authorizationPort.checkAccess(authenticationCredentials)) {
      throw new ForbiddenException("Access is denied, check your configuration");
    }
  }
}
