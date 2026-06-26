package com.github.mredjem.kafka.connect.extensions.api;

import com.github.mredjem.kafka.connect.extensions.dtos.ErrorDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiExceptionHandler {

  public static Response toErrorResponse(UriInfo uriInfo, Throwable exception) {
    log.warn("Error caught when calling {}", path(uriInfo), exception);

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
    error.setPath(path(uriInfo));
    error.setCode(status.getStatusCode());
    error.setReason(status.getReasonPhrase());
    error.setException(exception.getClass().getName());
    error.setMessage(getRootCauseMessage(exception));

    return error;
  }

  private static Response.Status status(Throwable exception) {
    if (ClientErrorException.class.isAssignableFrom(exception.getClass())) {
      return ((ClientErrorException) exception).getResponse().getStatusInfo().toEnum();
    }

    return Response.Status.INTERNAL_SERVER_ERROR;
  }

  private static String path(UriInfo uriInfo) {
    String path = uriInfo.getPath();

    return path.startsWith("/") ? path : "/" + path;
  }

  private static String getRootCauseMessage(Throwable exception) {
    Throwable cause = exception.getCause();

    if (cause != null) {
      return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }

    return exception.getClass().getSimpleName() + ": " + exception.getMessage();
  }
}
