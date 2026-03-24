package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.ScopedCredentials;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.mredjem.kafka.connect.ScopedCredentials.READ_SCOPE;
import static com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApiExceptionHandler.toErrorResponse;

public class BasicAuthFilter implements ContainerRequestFilter {

  private static final String SUPER_ADMINS_CONFIG = "super.admins";

  private final List<ScopedCredentials> superAdmins;

  private final ContainerRequestFilter next;

  private BasicAuthFilter(Map<String, String> configs, ContainerRequestFilter next) {
    this.superAdmins = Arrays.stream(configs.get(SUPER_ADMINS_CONFIG).split(","))
      .map(ScopedCredentials::of)
      .collect(Collectors.toList());
    this.next = next;
  }

  public static BasicAuthFilter create(Map<String, String> configs, ContainerRequestFilter next) {
    return new BasicAuthFilter(configs, next);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    if (FilterUtils.isInternalRequest(containerRequestContext) || FilterUtils.isAllowedAnonymously(containerRequestContext)) {
      return;
    }

    String basicCredentials = FilterUtils.getBasicCredentials(containerRequestContext);

    if (basicCredentials.isEmpty()) {
      this.next.filter(containerRequestContext);

      return;
    }

    ScopedCredentials superAdmin = this.findSuperAdmin(basicCredentials);

    if (superAdmin == null) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("Access is denied, check your configuration"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    if (!superAdmin.hasScope()) {
      return;
    }

    if (!READ_SCOPE.equalsIgnoreCase(superAdmin.getScope()) || FilterUtils.isWriteAccess(containerRequestContext)) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("Access is denied, check your configuration"));

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
