package com.github.mredjem.kafka.connect.units;

import com.github.mredjem.kafka.connect.oidc.ccloud.CelFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class CelFilterTest {

  @Test
  void testCelFilter() {
    CelFilter celFilter = CelFilter.parse("claims.iss == 'azure' && claims.aud == 'confluent'");

    Map<String, Object> variables = new HashMap<>();

    variables.put("iss", "azure");
    variables.put("aud", "confluent");

    Assertions.assertTrue(celFilter.evaluate(variables));

    variables.put("iss", "aws");

    Assertions.assertFalse(celFilter.evaluate(variables));
  }
}
