package com.github.mredjem.kafka.connect.oidc.azure.managed.dtos;

import java.util.List;

public class DataResponseDto<T> {

  private List<T> data;

  public List<T> getData() {
    return this.data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }
}
