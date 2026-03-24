package com.github.mredjem.kafka.connect.extensions.filters;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class FilterUtils {

  private static final Set<RequestMatcher> INTERNAL_REQUEST_MATCHERS = new HashSet<>();

  static {
    INTERNAL_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/([^/]+)/tasks/?")));
    INTERNAL_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/[^/]+/fence/?")));
  }

  private static final Set<RequestMatcher> ANONYMOUS_REQUEST_MATCHERS = new HashSet<>();

  static {
    ANONYMOUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?")));
    ANONYMOUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connector-plugins/?")));
  }

  private FilterUtils() {
  }

  public static boolean isInternalRequest(ContainerRequestContext containerRequestContext) {
    return INTERNAL_REQUEST_MATCHERS.stream().anyMatch(matcher -> matcher.test(containerRequestContext));
  }

  public static boolean isAllowedAnonymously(ContainerRequestContext containerRequestContext) {
    return ANONYMOUS_REQUEST_MATCHERS.stream().anyMatch(matcher -> matcher.test(containerRequestContext));
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
