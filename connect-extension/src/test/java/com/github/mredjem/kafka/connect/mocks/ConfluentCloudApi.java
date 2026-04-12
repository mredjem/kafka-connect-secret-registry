package com.github.mredjem.kafka.connect.mocks;

import com.github.mredjem.kafka.connect.SecretRegistryExtensionITCredentials;
import org.apache.commons.io.IOUtils;
import org.mockserver.integration.ClientAndServer;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ConfluentCloudApi {

  private ClientAndServer server;

  private ConfluentCloudApi() {}

  public static ConfluentCloudApi create() {
    return new ConfluentCloudApi();
  }

  public void start(int port) {
    this.server = ClientAndServer.startClientAndServer(port);
  }

  public boolean isRunning() {
    return this.server != null && this.server.isRunning();
  }

  public void stop() {
    if (this.isRunning()) {
      this.server.stop();
    }
  }

  public void initMocks() {
    this.server
      .when(
          request()
            .withMethod("GET")
            .withPath("/iam/v2/identity-providers")
            .withHeader(HttpHeaders.AUTHORIZATION, SecretRegistryExtensionITCredentials.CONFLUENT_CLOUD)
        )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/identity.providers.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/identity-providers/{identityProvider}/identity-pools")
          .withPathParameter("identityProvider", "dlz-f3a90de")
          .withHeader(HttpHeaders.AUTHORIZATION, SecretRegistryExtensionITCredentials.CONFLUENT_CLOUD)
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/identity.pools.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, SecretRegistryExtensionITCredentials.CONFLUENT_CLOUD)
          .withQueryStringParameter("principal", "User:pool-abc")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.organization.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, SecretRegistryExtensionITCredentials.CONFLUENT_CLOUD)
          .withQueryStringParameter("principal", "User:pool-abc")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.environment.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, SecretRegistryExtensionITCredentials.CONFLUENT_CLOUD)
          .withQueryStringParameter("principal", "User:pool-abc")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/cloud-cluster=lkc-123abc/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.cluster.json"))
      );
  }

  private String loadResource(String resourceName) {
    try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource " + resourceName + " not found");
      }

      return IOUtils.toString(is, StandardCharsets.UTF_8);

    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
