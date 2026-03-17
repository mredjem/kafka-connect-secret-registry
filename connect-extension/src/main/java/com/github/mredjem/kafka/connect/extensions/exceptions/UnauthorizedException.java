package com.github.mredjem.kafka.connect.extensions.exceptions;

import javax.ws.rs.core.Response;

public class UnauthorizedException extends RuntimeException implements HttpResponseStatus {

  public UnauthorizedException(String message) {
    super(message);
  }

  @Override
  public Response.Status status() {
    return Response.Status.UNAUTHORIZED;
  }
}
