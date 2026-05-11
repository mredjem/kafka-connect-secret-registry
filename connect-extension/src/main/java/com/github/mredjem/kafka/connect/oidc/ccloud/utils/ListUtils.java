package com.github.mredjem.kafka.connect.oidc.ccloud.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ListUtils {

  @SafeVarargs
  public <T> List<T> merge(List<T>... lists) {
    List<T> merged = new ArrayList<>();

    for (List<T> list : lists) {
      merged.addAll(list);
    }

    return merged;
  }
}
