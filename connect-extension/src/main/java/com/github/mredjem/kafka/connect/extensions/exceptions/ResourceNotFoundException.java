package com.github.mredjem.kafka.connect.extensions.exceptions;

import javax.ws.rs.core.Response;

public class ResourceNotFoundException extends RuntimeException implements HttpResponseStatus {

  public ResourceNotFoundException(String resourceName, String resourceId) {
    super(String.format("No %s found for id %s", resourceName, resourceId));
  }

  @Override
  public Response.Status status() {
    return Response.Status.NOT_FOUND;
  }
}
