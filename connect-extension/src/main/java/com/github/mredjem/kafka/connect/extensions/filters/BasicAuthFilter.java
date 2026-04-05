package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.ScopedCredentials;
import com.github.mredjem.kafka.connect.extensions.utils.FilterUtils;
import com.github.mredjem.kafka.connect.extensions.utils.RBACUtils;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

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
    this.superAdmins = Arrays.stream(ConfigUtils.getOrThrow(SUPER_ADMINS_CONFIG, configs).split(","))
      .map(ScopedCredentials::of)
      .collect(Collectors.toList());
    this.next = next;
  }

  public static BasicAuthFilter create(Map<String, String> configs, ContainerRequestFilter next) {
    return new BasicAuthFilter(configs, next);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    if (RBACUtils.isInternalRequest(containerRequestContext) || RBACUtils.isAllowedAnonymously(containerRequestContext)) {
      return;
    }

    String basicCredentials = FilterUtils.getBasicCredentials(containerRequestContext);

    if (basicCredentials.isEmpty()) {
      this.next.filter(containerRequestContext);

      return;
    }

    ScopedCredentials superAdmin = this.findSuperAdmin(basicCredentials);

    if (superAdmin == null) {
      Response errorResponse = toErrorResponse(containerRequestContext.getUriInfo(), new ForbiddenException("User is not a super admin"));

      containerRequestContext.abortWith(errorResponse);

      return;
    }

    if (!superAdmin.hasScope()) {
      return;
    }

    if (!READ_SCOPE.equalsIgnoreCase(superAdmin.getScope()) || RBACUtils.isWriteAccess(containerRequestContext)) {
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
