package com.github.mredjem.kafka.connect.extensions.rbac;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mredjem.kafka.connect.Operation;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class RbacRules {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final List<RequestMatcher> INTERNAL_REQUEST_MATCHERS = Arrays.asList(
    RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/[^/]+/tasks/?")),
    RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/[^/]+/fence/?"))
  );

  private static final List<RequestMatcher> ANONYMOUS_REQUEST_MATCHERS = Arrays.asList(
    RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?")),
    RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connector-plugins/?"))
  );

  private static final Map<Operation, List<RequestMatcher>> REQUEST_MATCHERS = new HashMap<>();

  static {
    REQUEST_MATCHERS.put(Operation.READ_CONFIGURATION, Arrays.asList(
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/config/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/offsets/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/topics/?"))
    ));

    REQUEST_MATCHERS.put(Operation.READ_STATUS, Arrays.asList(
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/status/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/tasks/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?connectors/([^/]+)/tasks/\\d+/status/?"))
    ));

    REQUEST_MATCHERS.put(Operation.READ_SECRET, Arrays.asList(
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/\\d+/?")),
      RequestMatcher.of(HttpMethod.GET, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/latest/?"))
    ));

    REQUEST_MATCHERS.put(Operation.PAUSE_RESUME_RESTART, Arrays.asList(
      RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/([^/]+)/tasks/\\d+/restart/?")),
      RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/([^/]+)/restart/?")),
      RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/pause/?")),
      RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/resume/?")),
      RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/stop/?"))
    ));

    REQUEST_MATCHERS.put(Operation.CONFIGURE, Arrays.asList(
      RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?connectors/?")),
      RequestMatcher.of(HttpMethod.PUT, Pattern.compile("/?connectors/([^/]+)/config/?")),
      RequestMatcher.of(HttpMethod.PATCH, Pattern.compile("/?connectors/([^/]+)/offsets/?")),
      RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?connectors/([^/]+)/offsets/?"))
    ));

    REQUEST_MATCHERS.put(Operation.CONFIGURE_SECRET, Collections.singletonList(
      RequestMatcher.of(HttpMethod.POST, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/?"))
    ));

    REQUEST_MATCHERS.put(Operation.DELETE, Collections.singletonList(
      RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?connectors/([^/]+)/?"))
    ));

    REQUEST_MATCHERS.put(Operation.DELETE_SECRET, Arrays.asList(
      RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/versions/\\d+/?")),
      RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?secret/paths/([^/]+)/keys/[^/]+/?")),
      RequestMatcher.of(HttpMethod.DELETE, Pattern.compile("/?secret/paths/([^/]+)/?"))
    ));
  }

  private RbacRules() {}

  public static boolean isInternalRequest(ContainerRequestContext containerRequestContext) {
    return INTERNAL_REQUEST_MATCHERS.stream().anyMatch(matcher -> matcher.test(containerRequestContext));
  }

  public static boolean isAllowedAnonymously(ContainerRequestContext containerRequestContext) {
    return ANONYMOUS_REQUEST_MATCHERS.stream().anyMatch(matcher -> matcher.test(containerRequestContext));
  }

  public static boolean isWriteAccess(ContainerRequestContext containerRequestContext) {
    List<RequestMatcher> readConfigurationMatchers = REQUEST_MATCHERS.get(Operation.READ_CONFIGURATION);

    if (readConfigurationMatchers.stream().anyMatch(predicate -> predicate.test(containerRequestContext))) {
      return false;
    }

    List<RequestMatcher> readStatusMatchers = REQUEST_MATCHERS.get(Operation.READ_STATUS);

    return readStatusMatchers.stream().noneMatch(predicate -> predicate.test(containerRequestContext));
  }

  public static RequestedAction getActionForRequest(ContainerRequestContext containerRequestContext) {
    for (Map.Entry<Operation, List<RequestMatcher>> e : REQUEST_MATCHERS.entrySet()) {
      Operation operation = e.getKey();

      List<RequestMatcher> matchers = e.getValue();

      String resourceName = matchers.stream()
        .filter(matcher -> matcher.test(containerRequestContext))
        .findFirst()
        .map(matcher -> {
          Function<ContainerRequestContext, String> matcherFn = getResourceName(operation, matcher);

          return matcherFn.apply(containerRequestContext);
        })
        .orElse("NONE");

      if (!"NONE".equalsIgnoreCase(resourceName)) {
        return RequestedAction.of(operation, resourceName);
      }
    }

    return null;
  }

  private static Function<ContainerRequestContext, String> getResourceName(Operation operation, RequestMatcher matcher) {
    return containerRequestContext -> {
      String foundResourceName = matcher.getResourceName(containerRequestContext);

      if (Operation.READ_CONFIGURATION == operation && foundResourceName.isEmpty()) {
        return "LIST_CONNECTOR_NAMES";
      }

      if (Operation.READ_SECRET == operation && foundResourceName.isEmpty()) {
        return "LIST_SECRET_PATHS";
      }

      String method = containerRequestContext.getMethod();

      if (Operation.CONFIGURE == operation && HttpMethod.POST.equalsIgnoreCase(method)) {
        return getConnectorNameFromBody(containerRequestContext);
      }

      return foundResourceName;
    };
  }

  private static String getConnectorNameFromBody(ContainerRequestContext containerRequestContext) {
    try {
      byte[] requestBodyBytes = toByteArray(containerRequestContext.getEntityStream());

      resetEntityStream(containerRequestContext, requestBodyBytes);

      Map<String, Object> requestBody = OBJECT_MAPPER.readValue(requestBodyBytes, new TypeReference<Map<String, Object>>() {});

      return (String) requestBody.get("name");

    } catch (final Exception ignored) {
      return "";
    }
  }

  private static byte[] toByteArray(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buffer = new byte[4096];
    int len;

    while ((len = inputStream.read(buffer)) > -1) {
      outputStream.write(buffer, 0, len);
    }

    outputStream.flush();

    return outputStream.toByteArray();
  }

  private static void resetEntityStream(ContainerRequestContext containerRequestContext, byte[] requestBodyBytes) {
    InputStream entityStreamCopy = new ByteArrayInputStream(requestBodyBytes);

    containerRequestContext.setEntityStream(entityStreamCopy);
  }
}
