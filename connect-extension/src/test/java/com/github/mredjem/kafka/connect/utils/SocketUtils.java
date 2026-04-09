package com.github.mredjem.kafka.connect.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

public final class SocketUtils {

  private SocketUtils() {}

  public static int nextAvailablePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();

    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
