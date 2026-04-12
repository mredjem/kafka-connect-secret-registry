package com.github.mredjem.kafka.connect.extensions.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mredjem.kafka.connect.Operation;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class RbacUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final Set<RequestMatcher> INTERNAL_REQUEST_MATCHERS = new HashSet<>();

  static {
    INTERNAL_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/[^/]+/tasks/?")));
    INTERNAL_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/[^/]+/fence/?")));
  }

  private static final Set<RequestMatcher> ANONYMOUS_REQUEST_MATCHERS = new HashSet<>();

  static {
    ANONYMOUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?")));
    ANONYMOUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connector-plugins/?")));
  }

  private static final Set<RequestMatcher> READ_CONFIGURATION_REQUEST_MATCHERS = new HashSet<>();

  static {
    READ_CONFIGURATION_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/?")));
    READ_CONFIGURATION_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/?")));
    READ_CONFIGURATION_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/config/?")));
    READ_CONFIGURATION_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/offsets/?")));
    READ_CONFIGURATION_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/topics/?")));
  }

  private static final Set<RequestMatcher> READ_STATUS_REQUEST_MATCHERS = new HashSet<>();

  static {
    READ_STATUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/status/?")));
    READ_STATUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/tasks/?")));
    READ_STATUS_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/tasks/\\d+/status/?")));
  }

  private static final Set<RequestMatcher> READ_SECRET_REQUEST_MATCHERS = new HashSet<>();

  static {
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/?")));
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/?")));
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/?")));
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/?")));
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/?")));
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/\\d+/?")));
    READ_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/latest/?")));
  }

  private static final Set<RequestMatcher> PAUSE_RESUME_RESTART_REQUEST_MATCHERS = new HashSet<>();

  static {
    PAUSE_RESUME_RESTART_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/([^/]+)/tasks/\\d+/restart/?")));
    PAUSE_RESUME_RESTART_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/([^/]+)/restart/?")));
    PAUSE_RESUME_RESTART_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/pause/?")));
    PAUSE_RESUME_RESTART_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/resume/?")));
    PAUSE_RESUME_RESTART_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/stop/?")));
  }

  private static final Set<RequestMatcher> CONFIGURE_REQUEST_MATCHERS = new HashSet<>();

  static {
    CONFIGURE_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/?")));
    CONFIGURE_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/config/?")));
    CONFIGURE_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.PATCH, Pattern.compile("/?connectors/([^/]+)/offsets/?")));
    CONFIGURE_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?connectors/([^/]+)/offsets/?")));
  }

  private static final RequestMatcher CONFIGURE_SECRET_REQUEST_MATCHERS = RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/?"));

  private static final RequestMatcher DELETE_REQUEST_MATCHER = RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?connectors/([^/]+)/?"));

  private static final Set<RequestMatcher> DELETE_SECRET_REQUEST_MATCHERS = new HashSet<>();

  static {
    DELETE_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/\\d+/?")));
    DELETE_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/?")));
    DELETE_SECRET_REQUEST_MATCHERS.add(RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?secret/paths/([^/]+)/?")));
  }

  private RbacUtils() {}

  public static boolean isInternalRequest(ContainerRequestContext containerRequestContext) {
    return INTERNAL_REQUEST_MATCHERS.stream().anyMatch(matcher -> matcher.test(containerRequestContext));
  }

  public static boolean isAllowedAnonymously(ContainerRequestContext containerRequestContext) {
    return ANONYMOUS_REQUEST_MATCHERS.stream().anyMatch(matcher -> matcher.test(containerRequestContext));
  }

  public static Operation getOperationForRequest(ContainerRequestContext containerRequestContext) {
    if (READ_CONFIGURATION_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return Operation.READ_CONFIGURATION;
    }

    if (READ_STATUS_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return Operation.READ_STATUS;
    }

    if (READ_SECRET_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return Operation.READ_SECRET;
    }

    if (PAUSE_RESUME_RESTART_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return Operation.PAUSE_RESUME_RESTART;
    }

    if (CONFIGURE_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return Operation.CONFIGURE;
    }

    if (CONFIGURE_SECRET_REQUEST_MATCHERS.test(containerRequestContext)) {
      return Operation.CONFIGURE_SECRET;
    }

    if (DELETE_REQUEST_MATCHER.test(containerRequestContext)) {
      return Operation.DELETE;
    }

    if (DELETE_SECRET_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return Operation.DELETE_SECRET;
    }

    return null;
  }

  public static String getResourceForRequest(ContainerRequestContext containerRequestContext, Operation operation) {
    if (Operation.READ_CONFIGURATION == operation) {
      String resourceName = getResourceName(containerRequestContext, READ_CONFIGURATION_REQUEST_MATCHERS);

      return resourceName.isEmpty() ? "LIST_CONNECTOR_NAMES" : resourceName;
    }

    if (Operation.READ_STATUS == operation) {
      return getResourceName(containerRequestContext, READ_STATUS_REQUEST_MATCHERS);
    }

    if (Operation.READ_SECRET == operation) {
      String resourceName = getResourceName(containerRequestContext, READ_SECRET_REQUEST_MATCHERS);

      return resourceName.isEmpty() ? "LIST_SECRET_PATHS" : resourceName;
    }

    if (Operation.PAUSE_RESUME_RESTART == operation) {
      return getResourceName(containerRequestContext, PAUSE_RESUME_RESTART_REQUEST_MATCHERS);
    }

    if (Operation.CONFIGURE == operation) {
      String requestMethod = containerRequestContext.getMethod();

      if (HttpMethod.POST.equals(requestMethod)) {
        return getConnectorNameFromBody(containerRequestContext);
      }

      return getResourceName(containerRequestContext, CONFIGURE_REQUEST_MATCHERS);
    }

    if (Operation.CONFIGURE_SECRET == operation) {
      return getResourceName(containerRequestContext, Collections.singleton(CONFIGURE_SECRET_REQUEST_MATCHERS));
    }

    if (Operation.DELETE == operation) {
      return getResourceName(containerRequestContext, Collections.singleton(DELETE_REQUEST_MATCHER));
    }

    if (Operation.DELETE_SECRET == operation) {
      return getResourceName(containerRequestContext, DELETE_SECRET_REQUEST_MATCHERS);
    }

    return "";
  }

  public static boolean isWriteAccess(ContainerRequestContext containerRequestContext) {
    if (READ_CONFIGURATION_REQUEST_MATCHERS.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return false;
    }

    return READ_STATUS_REQUEST_MATCHERS.stream().noneMatch(predicate -> predicate.test(containerRequestContext));
  }

  private static String getResourceName(ContainerRequestContext containerRequestContext, Set<RequestMatcher> requestMatchers) {
    return requestMatchers.stream()
      .map(predicate -> predicate.getResourceName(containerRequestContext))
      .filter(resourceName -> !resourceName.isEmpty())
      .findFirst()
      .orElse("");
  }

  private static String getConnectorNameFromBody(ContainerRequestContext containerRequestContext) {
    try {
      String rawBody = IOUtils.toString(containerRequestContext.getEntityStream());

      Map<String, Object> body = OBJECT_MAPPER.readValue(rawBody, new TypeReference<Map<String, Object>>() {});

      return (String) body.get("name");

    } catch (final Exception ignored) {
      return "";
    }
  }
}
