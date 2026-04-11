package com.github.mredjem.kafka.connect.extensions.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class IOUtils {

  private IOUtils() {}

  public static String toString(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buffer = new byte[1024];
    int len;

    while ((len = inputStream.read(buffer)) > -1) {
      outputStream.write(buffer, 0, len);
    }

    outputStream.flush();

    return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
  }
}
