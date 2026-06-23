package com.github.mredjem.kafka.connect.units;

import com.github.mredjem.kafka.connect.oidc.ccloud.CelFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CelFilterTest {

  @Test
  void shouldAllowForSimpleEqualities() {
    CelFilter celFilter = CelFilter.parse("""
      claims.iss == 'azure' && (claims.aud == 'confluent' || claims.aud == 'kafka') && claims.active != false && claims.ttl >= 30"""
    );

    Map<String, Object> confluentAudience = Map.of(
      "claims", Map.of(
        "iss", "azure",
        "aud", "confluent",
        "active", true,
        "ttl", 30
      )
    );

    Map<String, Object> kafkaAudience = Map.of(
      "claims", Map.of(
        "iss", "azure",
        "aud", "kafka",
        "active", true,
        "ttl", 60
      )
    );

    Map<String, Object> wrongIssuer = Map.of(
      "claims", Map.of(
        "iss", "aws",
        "aud", "confluent",
        "active", true,
        "ttl", 30
      )
    );

    Map<String, Object> wrongAudience = Map.of(
      "claims", Map.of(
        "iss", "azure",
        "aud", "asb",
        "active", true,
        "ttl", 30
      )
    );

    Map<String, Object> inactive = Map.of(
      "claims", Map.of(
        "iss", "azure",
        "aud", "confluent",
        "active", false,
        "ttl", 30
      )
    );

    Map<String, Object> shortTtl = Map.of(
      "claims", Map.of(
        "iss", "azure",
        "aud", "confluent",
        "active", true,
        "ttl", 20
      )
    );

    Assertions.assertTrue(celFilter.evaluate(confluentAudience));
    Assertions.assertTrue(celFilter.evaluate(kafkaAudience));
    Assertions.assertFalse(celFilter.evaluate(wrongIssuer));
    Assertions.assertFalse(celFilter.evaluate(wrongAudience));
    Assertions.assertFalse(celFilter.evaluate(inactive));
    Assertions.assertFalse(celFilter.evaluate(shortTtl));
  }
}
