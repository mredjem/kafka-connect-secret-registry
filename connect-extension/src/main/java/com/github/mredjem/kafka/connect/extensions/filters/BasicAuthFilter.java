package com.github.mredjem.kafka.connect.extensions.filters;

import com.github.mredjem.kafka.connect.extensions.exceptions.ForbiddenException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BasicAuthFilter implements ContainerRequestFilter {

  private static final String SUPER_ADMINS_CONFIG = "super.admins";

  private final List<String> superAdmins;

  private final ContainerRequestFilter next;

  private BasicAuthFilter(Map<String, String> configs, ContainerRequestFilter next) {
    this.superAdmins = Arrays.asList(configs.get(SUPER_ADMINS_CONFIG).split(","));
    this.next = next;
  }

  public static BasicAuthFilter create(Map<String, String> configs, ContainerRequestFilter next) {
    return new BasicAuthFilter(configs, next);
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    if (FilterUtils.isAllowedAnonymously(containerRequestContext)) {
      return;
    }

    String basicCredentials = FilterUtils.getBasicCredentials(containerRequestContext);

    if (basicCredentials.isEmpty()) {
      this.next.filter(containerRequestContext);

      return;
    }

    if (!this.superAdmins.contains(basicCredentials)) {
      throw new ForbiddenException("Access is denied, check your configuration");
    }
  }
}
