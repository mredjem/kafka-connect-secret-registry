package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.extensions.dtos.ErrorDto;
import com.github.mredjem.kafka.connect.extensions.exceptions.HttpResponseStatus;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.time.Instant;

public class SecretRegistryExceptionHandler implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable exception) {
    ErrorDto error = toError(exception);

    return Response.status(error.getCode())
      .entity(error)
      .build();
  }

  private ErrorDto toError(Throwable exception) {
    Response.Status status = status(exception);

    return toError(exception, status);
  }

  private ErrorDto toError(Throwable exception, Response.Status status) {
    ErrorDto error = new ErrorDto();

    error.setTimestamp(Instant.now().toEpochMilli());
    error.setPath("");
    error.setCode(status.getStatusCode());
    error.setReason(status.getReasonPhrase());
    error.setException(exception.getClass().getName());
    error.setMessage(exception.getMessage());

    return error;
  }

  private Response.Status status(Throwable exception) {
    if (HttpResponseStatus.class.isAssignableFrom(exception.getClass())) {
      return ((HttpResponseStatus) exception).status();
    }

    return Response.Status.INTERNAL_SERVER_ERROR;
  }
}
