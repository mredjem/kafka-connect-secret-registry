package com.github.mredjem.kafka.connect.mocks;

import com.github.mredjem.kafka.connect.Credentials;
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

  public void initMocks() {
    this.server.reset();

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/api-keys/QRSTUVWXYZABCDEF")
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/api.key.json"))
      );

    this.server
      .when(
          request()
            .withMethod("GET")
            .withPath("/iam/v2/identity-providers")
            .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
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
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
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
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
          .withQueryStringParameter("principal", "User:pool-abc")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.organization.identity.pool.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
          .withQueryStringParameter("principal", "User:sa-12345")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.organization.service.account.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
          .withQueryStringParameter("principal", "User:pool-abc")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.environment.identity.pool.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
          .withQueryStringParameter("principal", "User:sa-12345")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.environment.service.account.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
          .withQueryStringParameter("principal", "User:pool-abc")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/cloud-cluster=lkc-123abc/connector=*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.cluster.identity.pool.json"))
      );

    this.server
      .when(
        request()
          .withMethod("GET")
          .withPath("/iam/v2/role-bindings")
          .withHeader(HttpHeaders.AUTHORIZATION, Credentials.confluentCloud())
          .withQueryStringParameter("principal", "User:sa-12345")
          .withQueryStringParameter("crn_pattern", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/cloud-cluster=lkc-123abc/connector=*")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .withBody(this.loadResource("mocks/role.bindings.cluster.service.account.json"))
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
