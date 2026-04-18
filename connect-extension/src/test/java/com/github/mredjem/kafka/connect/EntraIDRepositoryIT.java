package com.github.mredjem.kafka.connect;

import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.azure.EntraIDRepository;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import com.github.mredjem.kafka.connect.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class EntraIDRepositoryIT extends AbstractIT {

  private static OidcPort oidcPort;

  @BeforeAll
  static void setupEntraIDRepository() {
    Map<String, String> configuration = TestUtils.load();

    Map<String, String> extensionConfiguration = ConfigUtils.addEntry(
      ConfigUtils.configsForPrefix("config.providers.secret.param.kafkastore.", configuration),
      "bootstrap.servers",
      "localhost:" + KAFKA.getMappedPort(9092)
    );

    oidcPort = EntraIDRepository.create(extensionConfiguration);
  }

  @Test
  void shouldOnlyAcceptBearerCredentials() {
    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(Credentials.ci());

    boolean validCredentials = oidcPort.validateCredentials(authenticationCredentials);

    Assertions.assertFalse(validCredentials);
  }

  @Test
  void shouldExtractRolesFromClaims() {
    Set<String> roles = new HashSet<>(Arrays.asList(
      "ConnectManager",
      "DeveloperWrite"
    ));

    AuthenticationCredentials authenticationCredentials = AuthenticationCredentials.of(Credentials.servicePrincipal(roles));

    boolean validCredentials = oidcPort.validateCredentials(authenticationCredentials);

    Assertions.assertTrue(validCredentials);

    Map<String, RoleBinding> roleBindings = oidcPort.getRoleBindings(authenticationCredentials)
      .stream()
      .collect(Collectors.toMap(
        roleBinding -> roleBinding.getRole().roleName(),
        Function.identity()
      ));

    Assertions.assertEquals(2, roleBindings.size());

    Assertions.assertTrue(roles.contains("ConnectManager"));
    Assertions.assertTrue(roles.contains("DeveloperWrite"));

    roleBindings.forEach((key, value) -> Assertions.assertTrue(value.getResourceScope().matches("*")));
  }
}
