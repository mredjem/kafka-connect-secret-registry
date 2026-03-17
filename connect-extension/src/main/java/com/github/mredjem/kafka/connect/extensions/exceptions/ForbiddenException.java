package com.github.mredjem.kafka.connect.extensions.exceptions;

import javax.ws.rs.core.Response;

public class ForbiddenException extends RuntimeException implements HttpResponseStatus {

  public ForbiddenException(String message) {
    super(message);
  }

  @Override
  public Response.Status status() {
    return Response.Status.FORBIDDEN;
  }
}
