package com.github.mredjem.kafka.connect.extensions.rbac;

import lombok.RequiredArgsConstructor;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(staticName = "of")
public class RequestMatcher implements Predicate<ContainerRequestContext> {

  private final String method;
  private final Pattern path;

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
