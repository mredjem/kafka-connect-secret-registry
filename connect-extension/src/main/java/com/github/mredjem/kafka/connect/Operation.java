package com.github.mredjem.kafka.connect;

public enum Operation {

  READ_CONFIGURATION,
  READ_STATUS,
  READ_SECRET,
  PAUSE_RESUME_RESTART,
  CONFIGURE,
  CONFIGURE_SECRET,
  DELETE,
  DELETE_SECRET,
}
