package com.github.mredjem.kafka.connect.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class HostnameUtils {

  private HostnameUtils() {}

  public static String hostname() {
    String hostname = System.getenv("HOSTNAME");

    if (hostname != null && !hostname.isEmpty()) {
      return hostname;
    }

    String computerName = System.getenv("COMPUTERNAME");

    if (computerName != null && !computerName.isEmpty()) {
      return computerName;
    }

    String hostnameFromLocalhost = hostnameFromLocalhost();

    if (hostnameFromLocalhost != null && !hostnameFromLocalhost.isEmpty()) {
      return hostnameFromLocalhost;
    }

    throw new IllegalStateException("Unable to determine hostname");
  }

  private static String hostnameFromLocalhost() {
    try {
      InetAddress localIpAddress = InetAddress.getLocalHost();

      return localIpAddress.getHostName();

    } catch (final UnknownHostException ignored) {
      return "";
    }
  }
}
