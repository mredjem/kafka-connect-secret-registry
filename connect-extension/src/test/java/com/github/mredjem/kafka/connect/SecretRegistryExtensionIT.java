package com.github.mredjem.kafka.connect;

import com.github.mredjem.kafka.connect.dtos.CreateConnectorDto;
import com.github.mredjem.kafka.connect.extensions.dtos.CreateSecretDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class SecretRegistryExtensionIT extends AbstractIT {

  @Test
  void shouldBeRejectedIfUnauthenticated() {
    given()
      .header(HttpHeaders.ACCEPT, "application/json")
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(401);

    given()
      .header(HttpHeaders.ACCEPT, "application/json")
    .when()
      .get("/connectors")
    .then()
      .statusCode(401);
  }

  @Test
  void shouldBeRejectedIfAPIKeyDoesNotExist() {
    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.notFound())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(403);
  }

  @Test
  void shouldAllowGettingStateWhenUnauthenticated() {
    given()
      .header(HttpHeaders.ACCEPT, "application/json")
    .when()
      .get("/")
    .then()
      .statusCode(200);
  }

  @Test
  void shouldAllowListingPluginsWhenUnauthenticated() {
    given()
      .header(HttpHeaders.ACCEPT, "application/json")
    .when()
      .get("/connector-plugins")
    .then()
      .statusCode(200);
  }

  @Test
  void shouldAllowReadingConfigurationAndStatusWhenDeveloperWrite() {
    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
      .body("{ \"name\": \"my_datagen_connector\" }")
    .when()
      .post("/connectors")
    .then()
      .statusCode(500);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(200);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_datagen_connector/config")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_datagen_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_datagen_connector/tasks")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .delete("/connectors/my_datagen_connector")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_mirror_connector/config")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_mirror_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_mirror_connector/tasks")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .delete("/connectors/my_mirror_connector")
    .then()
      .statusCode(403);
  }

  @Test
  void shouldAllowReadingStatusAndSecretAndRestartingConnectorsWhenConnectManager() {
    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
      .body("{}")
    .when()
      .post("/connectors")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(200);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_mirror_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
    .when()
      .post("/connectors/my_mirror_connector/restart")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
    .when()
      .delete("/connectors/my_mirror_connector")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_datagen_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
    .when()
      .post("/connectors/my_datagen_connector/restart")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
    .when()
      .delete("/connectors/my_datagen_connector")
    .then()
      .statusCode(403);
  }

  @Test
  void shouldAllowReadingStatusAndSecretAndRestartingConnectorsWhenConnectManagerUsingAPIKey() {
    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
      .contentType(ContentType.JSON)
      .body("{}")
    .when()
      .post("/connectors")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(200);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
    .when()
      .get("/connectors/my_mirror_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
      .contentType(ContentType.JSON)
    .when()
      .post("/connectors/my_mirror_connector/restart")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
      .contentType(ContentType.JSON)
    .when()
      .delete("/connectors/my_mirror_connector")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
    .when()
      .get("/connectors/my_datagen_connector/status")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
      .contentType(ContentType.JSON)
    .when()
      .post("/connectors/my_datagen_connector/restart")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.ci())
      .contentType(ContentType.JSON)
    .when()
      .delete("/connectors/my_datagen_connector")
    .then()
      .statusCode(403);
  }

  @Test
  void shouldAllowReadingStatusAndRestartingConnectorsWhenEnvironmentOperator() {
    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_mirror_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
    .when()
      .post("/connectors/my_mirror_connector/restart")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
    .when()
      .get("/connectors/my_datagen_connector/status")
    .then()
      .statusCode(404);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.servicePrincipal())
      .contentType(ContentType.JSON)
    .when()
      .post("/connectors/my_datagen_connector/restart")
    .then()
      .statusCode(404);
  }

  @Test
  void shouldCreateNewSecrets() {
    CreateSecretDto createPgUserSecret = CreateSecretDto.of("admin");

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .pathParam("path", "dev.users.postgres.jdbc-sink-connector")
      .pathParam("key", "pg.user")
      .body(createPgUserSecret)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("path", is("dev.users.postgres.jdbc-sink-connector"))
      .body("key", is("pg.user"))
      .body("version", is(1))
      .body("secret", is(createPgUserSecret.getSecret()));

    CreateSecretDto createPgPasswordSecret = CreateSecretDto.of("password");

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .pathParam("path", "dev.users.postgres.jdbc-sink-connector")
      .pathParam("key", "pg.password")
      .body(createPgPasswordSecret)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("path", is("dev.users.postgres.jdbc-sink-connector"))
      .body("key", is("pg.password"))
      .body("version", is(1))
      .body("secret", is(createPgPasswordSecret.getSecret()));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("$", hasItem("dev.users.postgres.jdbc-sink-connector"));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .pathParam("path", "dev.users.postgres.jdbc-sink-connector")
    .when()
      .get("/secret/paths/{path}/keys")
    .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("$", hasSize(2))
      .body("$", hasItems("pg.user", "pg.password"));
  }

  @Test
  void shouldGetLatestSecretVersion() {
    CreateSecretDto createOracleUserV1 = CreateSecretDto.of("admin1");

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .pathParam("path", "dev.users.oracle.jdbc-sink-connector")
      .pathParam("key", "oracle.user")
      .body(createOracleUserV1)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("path", is("dev.users.oracle.jdbc-sink-connector"))
      .body("key", is("oracle.user"))
      .body("version", is(1))
      .body("secret", is(createOracleUserV1.getSecret()));

    CreateSecretDto createOracleUserV2 = CreateSecretDto.of("admin2");

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .pathParam("path", "dev.users.oracle.jdbc-sink-connector")
      .pathParam("key", "oracle.user")
      .body(createOracleUserV2)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("path", is("dev.users.oracle.jdbc-sink-connector"))
      .body("key", is("oracle.user"))
      .body("version", is(2))
      .body("secret", is(createOracleUserV2.getSecret()));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .pathParam("path", "dev.users.oracle.jdbc-sink-connector")
      .pathParam("key", "oracle.user")
    .when()
      .get("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("$", hasSize(2))
      .body("$", hasItems(1, 2));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .pathParam("path", "dev.users.oracle.jdbc-sink-connector")
      .pathParam("key", "oracle.user")
      .pathParam("version", "latest")
    .when()
      .get("/secret/paths/{path}/keys/{key}/versions/{version}")
    .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("path", is("dev.users.oracle.jdbc-sink-connector"))
      .body("key", is("oracle.user"))
      .body("version", is(2))
      .body("secret", is(createOracleUserV2.getSecret()));
  }

  @Test
  void shouldDeleteSecret() {
    CreateSecretDto createSqlServerUserSecret = CreateSecretDto.of("admin");

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .pathParam("path", "prd.users.mssql.jdbc-sink-connector")
      .pathParam("key", "mssql.user")
      .body(createSqlServerUserSecret)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("path", is("prd.users.mssql.jdbc-sink-connector"))
      .body("key", is("mssql.user"))
      .body("version", is(1))
      .body("secret", is(createSqlServerUserSecret.getSecret()));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .pathParam("path", "prd.users.mssql.jdbc-sink-connector")
      .pathParam("key", "mssql.user")
      .pathParam("version", "1")
    .when()
      .delete("/secret/paths/{path}/keys/{key}/versions/{version}")
    .then()
      .statusCode(204);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .pathParam("path", "prd.users.mssql.jdbc-sink-connector")
      .pathParam("key", "mssql.user")
    .when()
      .get("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("$", empty());
  }

  @Test
  void shouldInjectSecretInConnectorConfiguration() {
    CreateSecretDto createTestConnectorSecret = CreateSecretDto.of("-1");

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .pathParam("path", "prd-connector")
      .pathParam("key", "tasks.max")
      .body(createTestConnectorSecret)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON);

    CreateConnectorDto createTestConnectorDto = CreateConnectorDto.createDummy();

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.organizationAdmin())
      .contentType(ContentType.JSON)
      .body(createTestConnectorDto)
    .when()
      .post("/connectors")
    .then()
      .statusCode(400)
      .contentType(ContentType.JSON)
      .body("message", containsString("Invalid value -1 for configuration tasks.max"));
  }
}
