package com.github.mredjem.kafka.connect.extensions.filters;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public final class FilterUtils {

  private static final Set<String> ALLOWED_PATHS = new HashSet<>();

  static {
    ALLOWED_PATHS.add("");
    ALLOWED_PATHS.add("/");
    ALLOWED_PATHS.add("connector-plugins");
    ALLOWED_PATHS.add("connector-plugins/");
    ALLOWED_PATHS.add("/connector-plugins");
    ALLOWED_PATHS.add("/connector-plugins/");
  }

  private FilterUtils() {
  }

  public static boolean isAllowedAnonymously(ContainerRequestContext containerRequestContext) {
    String requestMethod = containerRequestContext.getMethod();

    if (!HttpMethod.GET.equalsIgnoreCase(requestMethod)) {
      return false;
    }

    String requestPath = containerRequestContext.getUriInfo().getPath();

    return ALLOWED_PATHS.contains(requestPath.toLowerCase());
  }

  public static boolean isWriteAccess(ContainerRequestContext containerRequestContext) {
    String requestMethod = containerRequestContext.getMethod();

    if (!HttpMethod.GET.equalsIgnoreCase(requestMethod)) {
      return true;
    }

    String requestPath = containerRequestContext.getUriInfo().getPath().toLowerCase();

    return requestPath.startsWith("secret") || requestPath.startsWith("/secret");
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
    String authorization = containerRequestContext.getHeaderString("Authorization");

    if (authorization == null || authorization.isEmpty()) {
      return "";
    }

    return authorization;
  }
}
