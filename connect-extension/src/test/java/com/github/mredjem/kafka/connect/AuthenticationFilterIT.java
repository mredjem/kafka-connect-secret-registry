package com.github.mredjem.kafka.connect;

import com.github.mredjem.kafka.connect.extensions.dtos.ErrorDto;
import com.github.mredjem.kafka.connect.extensions.filters.AuthenticationFilter;
import com.github.mredjem.kafka.connect.internals.KafkaAuthorizationRepository;
import com.github.mredjem.kafka.connect.mocks.MockContainerRequestContext;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.azure.ccloud.ConfluentCloudRepository;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import com.github.mredjem.kafka.connect.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AuthenticationFilterIT extends AbstractIT {

  private static AuthenticationFilter authenticationFilter;

  @BeforeAll
  static void setupAuthenticationFilter() {
    Map<String, String> configuration = TestUtils.load();

    Map<String, String> oidcConfiguration = ConfigUtils.configsForPrefix(OidcConfigs.OIDC_PREFIX, configuration);
    Map<String, String> extensionConfiguration = ConfigUtils.configsForPrefix("config.providers.secret.param.", configuration);

    OidcPort oidcPort = ConfluentCloudRepository.create(ConfigUtils.addEntry(
      oidcConfiguration,
      OidcConfigs.API_BASE_URL_CONFIG, "http://localhost:" + MOCKSERVER_PORT
    ));

    AuthorizationPort authorizationPort = KafkaAuthorizationRepository.create(oidcPort);

    authenticationFilter = AuthenticationFilter.create(extensionConfiguration, authorizationPort);
  }

  @Test
  void shouldBeRejectedIfUnauthenticated() throws IOException {
    Map<String, String> headers = this.defaultHeaders();

    MockContainerRequestContext connectorsRequest = MockContainerRequestContext.of(HttpMethod.GET, "/connectors", headers);

    connectorsRequest.addAssertion(response -> {
      Response.Status expectedStatus = Response.Status.UNAUTHORIZED;

      Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
      Assertions.assertInstanceOf(ErrorDto.class, response.getEntity());

      ErrorDto error = (ErrorDto) response.getEntity();

      Assertions.assertEquals(expectedStatus.getStatusCode(), error.getCode());
      Assertions.assertEquals(expectedStatus.getReasonPhrase(), error.getReason());
      Assertions.assertEquals("/connectors", error.getPath());
    });

    authenticationFilter.filter(connectorsRequest);
  }

  @Test
  void shouldAllowGettingStateWhenUnauthenticated() {
    Map<String, String> headers = this.defaultHeaders();

    MockContainerRequestContext nodeRequest = MockContainerRequestContext.of(HttpMethod.GET, "/", headers);

    nodeRequest.addAssertion(response -> {
      Response.Status expectedStatus = Response.Status.OK;

      Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
    });
  }

  @Test
  void shouldAllowListingPluginsWhenUnauthenticated() {
    Map<String, String> headers = this.defaultHeaders();

    MockContainerRequestContext pluginsRequest = MockContainerRequestContext.of(HttpMethod.GET, "/connector-plugins", headers);

    pluginsRequest.addAssertion(response -> {
      Response.Status expectedStatus = Response.Status.OK;

      Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
    });
  }

  @Test
  void shouldRespectSuperAdminScope() throws IOException {
    Map<String, String> headers = ConfigUtils.addEntry(this.defaultHeaders(), HttpHeaders.AUTHORIZATION, Credentials.centreon());

    MockContainerRequestContext connectorsRequest = MockContainerRequestContext.of(HttpMethod.GET, "/connectors", headers);

    connectorsRequest.addAssertion(response -> {
      Response.Status expectedStatus = Response.Status.FORBIDDEN;

      Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
      Assertions.assertInstanceOf(ErrorDto.class, response.getEntity());

      ErrorDto error = (ErrorDto) response.getEntity();

      Assertions.assertEquals(expectedStatus.getStatusCode(), error.getCode());
      Assertions.assertEquals(expectedStatus.getReasonPhrase(), error.getReason());
      Assertions.assertEquals("/connectors", error.getPath());
    });

    authenticationFilter.filter(connectorsRequest);
  }

  @Test
  void shouldAllowReadingConfigurationAndStatusWhenDeveloperWrite() throws IOException {
    Map<String, String> headers = ConfigUtils.addEntry(this.defaultHeaders(), HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal());

    MockContainerRequestContext createRequest = MockContainerRequestContext.of(
      HttpMethod.POST,
      "/connectors",
      headers,
      "{ \"name\": \"my_datagen_connector\" }"
    );

    createRequest.addAssertion(response -> Assertions.fail());

    authenticationFilter.filter(createRequest);

    MockContainerRequestContext deleteRequest = MockContainerRequestContext.of(
      HttpMethod.DELETE,
      "/connectors/my_datagen_connector",
      headers
    );

    deleteRequest.addAssertion(response -> {
      Response.Status expectedStatus = Response.Status.FORBIDDEN;

      Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
    });

    authenticationFilter.filter(deleteRequest);
  }

  @Test
  void shouldAllowReadingStatusAndSecretAndRestartingConnectorsWhenConnectManager() throws IOException {
    Map<String, String> headers = ConfigUtils.addEntry(this.defaultHeaders(), HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal());

    MockContainerRequestContext statusRequest = MockContainerRequestContext.of(
      HttpMethod.GET,
      "/connectors/my_mirror_connector/status",
      headers
    );

    statusRequest.addAssertion(response -> Assertions.fail());

    authenticationFilter.filter(statusRequest);

    MockContainerRequestContext restartRequest = MockContainerRequestContext.of(
      HttpMethod.POST,
      "/connectors/my_mirror_connector/restart",
      headers
    );

    restartRequest.addAssertion(response -> Assertions.fail());

    authenticationFilter.filter(restartRequest);

    MockContainerRequestContext createRequest = MockContainerRequestContext.of(
      HttpMethod.POST,
      "/connectors",
      headers,
      "{ \"name\": \"my_mirror_connector\" }"
    );

    createRequest.addAssertion(response -> {
      Response.Status expectedStatus = Response.Status.FORBIDDEN;

      Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
    });

    authenticationFilter.filter(createRequest);
  }

  @Test
  void shouldAllowReadingStatusAndSecretAndRestartingConnectorsWhenConnectManagerUsingAPIKey() throws IOException {
    Map<String, String> headers = ConfigUtils.addEntry(this.defaultHeaders(), HttpHeaders.AUTHORIZATION, Credentials.ci());

    MockContainerRequestContext secretRequest = MockContainerRequestContext.of(
      HttpMethod.GET,
      "/secret/paths",
      headers
    );

    secretRequest.addAssertion(response -> Assertions.fail());

    authenticationFilter.filter(secretRequest);

    MockContainerRequestContext statusRequest = MockContainerRequestContext.of(
      HttpMethod.GET,
      "/connectors/my_mirror_connector/status",
      headers
    );

    statusRequest.addAssertion(response -> Assertions.fail());

    authenticationFilter.filter(statusRequest);

    MockContainerRequestContext restartRequest = MockContainerRequestContext.of(
      HttpMethod.POST,
      "/connectors/my_mirror_connector/restart",
      headers
    );

    restartRequest.addAssertion(response -> Assertions.fail());

    authenticationFilter.filter(restartRequest);
  }

  private Map<String, String> defaultHeaders() {
    Map<String, String> headers = new HashMap<>();

    headers.put(HttpHeaders.ACCEPT, "application/json");
    headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

    return headers;
  }
}
