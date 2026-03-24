package com.github.mredjem.kafka.connect.extensions.filters;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.function.Predicate;
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

    return this.path.matcher(containerRequestContext.getUriInfo().getPath()).matches();
  }
}
