package com.github.mredjem.kafka.connect.oidc.ccloud.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataResponseDto<T> {

  private List<T> data;
}
