package com.github.mredjem.kafka.connect.extensions.rbac;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMatcher implements Predicate<ContainerRequestContext> {

  private final String method;
  private final Pattern path;

  private RequestMatcher(String method, Pattern path) {
    this.method = method;
    this.path = path;
  }

  public static RequestMatcher of(String method, Pattern path) {
    return new RequestMatcher(method, path);
  }

  @Override
  public boolean test(ContainerRequestContext containerRequestContext) {
    if (!containerRequestContext.getMethod().equals(method)) {
      return false;
    }

    return path.matcher(containerRequestContext.getUriInfo().getPath()).matches();
  }

  public String getResourceName(ContainerRequestContext containerRequestContext) {
    Matcher matcher = path.matcher(containerRequestContext.getUriInfo().getPath());

    if (!matcher.matches()) {
      return "";
    }

    return matcher.groupCount() == 1 ? matcher.group(1) : "";
  }
}
