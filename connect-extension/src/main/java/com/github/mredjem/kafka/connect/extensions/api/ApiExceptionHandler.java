package com.github.mredjem.kafka.connect.extensions.api;

import com.github.mredjem.kafka.connect.extensions.dtos.ErrorDto;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;

public final class ApiExceptionHandler {

  private ApiExceptionHandler() {}

  public static Response toErrorResponse(UriInfo uriInfo, Throwable exception) {
    ErrorDto error = toError(uriInfo, exception);

    return Response.status(error.getCode())
      .entity(error)
      .build();
  }

  private static ErrorDto toError(UriInfo uriInfo, Throwable exception) {
    Response.Status status = status(exception);

    return toError(uriInfo, exception, status);
  }

  private static ErrorDto toError(UriInfo uriInfo, Throwable exception, Response.Status status) {
    ErrorDto error = new ErrorDto();

    error.setTimestamp(Instant.now().toEpochMilli());
    error.setPath(uriInfo.getPath());
    error.setCode(status.getStatusCode());
    error.setReason(status.getReasonPhrase());
    error.setException(exception.getClass().getName());
    error.setMessage(exception.getMessage());

    return error;
  }

  private static Response.Status status(Throwable exception) {
    if (ClientErrorException.class.isAssignableFrom(exception.getClass())) {
      return ((ClientErrorException) exception).getResponse().getStatusInfo().toEnum();
    }

    return Response.Status.INTERNAL_SERVER_ERROR;
  }
}
