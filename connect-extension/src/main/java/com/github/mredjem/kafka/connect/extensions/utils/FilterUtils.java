package com.github.mredjem.kafka.connect.extensions.utils;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import java.util.Base64;

public final class FilterUtils {

  private FilterUtils() {
  }

  public static String getBasicCredentials(ContainerRequestContext containerRequestContext) {
    String authorization = getAuthorizationValue(containerRequestContext);

    if (!authorization.startsWith("Basic ")) {
      return "";
    }

    String basicCredentials = authorization.substring("Basic ".length());

    return new String(Base64.getDecoder().decode(basicCredentials));
  }

  public static String getBearerCredentials(ContainerRequestContext containerRequestContext) {
    String authorization = getAuthorizationValue(containerRequestContext);

    if (!authorization.startsWith("Bearer ")) {
      return "";
    }

    return authorization.substring("Bearer ".length());
  }

  private static String getAuthorizationValue(ContainerRequestContext containerRequestContext) {
    String authorization = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

    if (authorization == null || authorization.isEmpty()) {
      return "";
    }

    return authorization;
  }
}
