package com.github.mredjem.kafka.connect.extensions.exceptions;

import javax.ws.rs.core.Response;

public interface HttpResponseStatus {

  Response.Status status();
}
