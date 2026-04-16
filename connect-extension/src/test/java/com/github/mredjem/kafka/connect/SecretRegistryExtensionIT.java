package com.github.mredjem.kafka.connect;

import com.github.mredjem.kafka.connect.containers.ConfluentKafkaConnectContainer;
import com.github.mredjem.kafka.connect.dtos.CreateConnectorDto;
import com.github.mredjem.kafka.connect.extensions.SecretRegistryExtension;
import com.github.mredjem.kafka.connect.extensions.dtos.CreateSecretDto;
import com.github.mredjem.kafka.connect.mocks.ConfluentCloudApi;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigProvider;
import com.github.mredjem.kafka.connect.utils.SocketUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import javax.ws.rs.core.HttpHeaders;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Testcontainers
class SecretRegistryExtensionIT {

  private static final int MOCKSERVER_PORT = SocketUtils.nextAvailablePort();

  private static final Network NETWORK = Network.newNetwork();

  private static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.0")
    .withNetwork(NETWORK)
    .withNetworkAliases("kafka")
    .withExposedPorts(9092, 9093, 29092)
    .withListener("kafka:29092")
    .withEnv("KAFKA_NODE_ID", "1")
    .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false")
    .withEnv("CONFLUENT_METRICS_ENABLE", "false");

  private static final ConfluentKafkaConnectContainer CONNECT = new ConfluentKafkaConnectContainer("confluentinc/cp-kafka-connect-base:7.7.0")
    .withNetwork(NETWORK)
    .withNetworkAliases("connect")
    .withExposedPorts(8083)
    .withEnv("CONNECT_BOOTSTRAP_SERVERS", "kafka:29092")
    .withEnv("CONNECT_GROUP_ID", "connect")
    .withEnv("CONNECT_CONFIG_STORAGE_TOPIC", "connect-configs")
    .withEnv("CONNECT_OFFSET_STORAGE_TOPIC", "connect-offsets")
    .withEnv("CONNECT_STATUS_STORAGE_TOPIC", "connect-status")
    .withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1")
    .withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1")
    .withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1")
    .withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
    .withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
    .withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "connect")
    .withEnv("CONNECT_REST_PORT", "8083")
    .withEnv("CONNECT_PLUGIN_PATH", ConfluentKafkaConnectContainer.PLUGIN_PATH)
    .withEnv("CONNECT_REST_EXTENSION_CLASSES", SecretRegistryExtension.class.getName())
    .withEnv("CONNECT_CONFLUENT_CLOUD_CLUSTER_CRN_PATTERN", "crn://confluent.cloud/organization=9bb441c4-edef-46ac-8a41-c49e44a3fd9a/environment=env-456xy/cloud-cluster=lkc-123abc")
    .withEnv("CONNECT_CONFLUENT_CLOUD_API_BASE_URL", "http://host.testcontainers.internal:" + MOCKSERVER_PORT)
    .withEnv("CONNECT_CONFLUENT_CLOUD_API_KEY", "ABCDEFGHIJKLMNOP")
    .withEnv("CONNECT_CONFLUENT_CLOUD_API_SECRET", "R15hoiDIq8Nxu/lY4mPO3DwAVIfU5W7OI+efsB607mLgHTnVW5XJGVqX2ysDx987")
    .withEnv("CONNECT_CONFLUENT_CLOUD_IDENTITY_PROVIDER_NAME", "azure")
    .withEnv("CONNECT_CONFIG_PROVIDERS", "secret")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_CLASS", InternalSecretConfigProvider.class.getName())
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:29092")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_KAFKASTORE_TOPIC", "_connect-secrets")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_KAFKASTORE_TOPIC_REPLICATION_FACTOR", "1")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_MASTER_ENCRYPTION_KEY", "juby895fmddr5hw58839d3myz27zw206ffxiv68m")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_SECRET_REGISTRY_GROUP_ID", "secret-registry")
    .withEnv("CONNECT_CONFIG_PROVIDERS_SECRET_PARAM_SUPER_ADMINS", "admin:password:,centreon:password:read")
    .withLogConsumer(outputFrame -> new Slf4jLogConsumer(LoggerFactory.getLogger("connect")).accept(outputFrame))
    .dependsOn(KAFKA);

  private static final ConfluentCloudApi CONFLUENT_CLOUD_API = ConfluentCloudApi.create();

  @BeforeAll
  static void setUp() throws InterruptedException {
    CONFLUENT_CLOUD_API.start(MOCKSERVER_PORT);

    Awaitility.await().atMost(5L, TimeUnit.SECONDS).until(CONFLUENT_CLOUD_API::isRunning);

    org.testcontainers.Testcontainers.exposeHostPorts(MOCKSERVER_PORT);

    KAFKA.start();

    CONNECT.start();
    CONNECT
      .waitingFor(Wait.forHealthcheck())
      .waitingFor(Wait.forHttp("/connector-plugins"))
      .waitingFor(Wait.forLogMessage("server is started and ready to handle requests", 1));

    Thread.sleep(3_000L);

    RestAssured.baseURI = "http://localhost:" + CONNECT.getMappedPort(8083);
  }

  @BeforeEach
  void beforeEach() {
    CONFLUENT_CLOUD_API.initMocks();
  }

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
  void shouldRespectSuperAdminScope() {
    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.centreon())
    .when()
      .get("/connectors")
    .then()
      .statusCode(200);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.centreon())
      .contentType(ContentType.JSON)
      .body("{}")
    .when()
      .post("/connectors")
    .then()
      .statusCode(403);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.centreon())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(403);
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
    .when()
      .get("/secret/paths")
    .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("$", hasItem("dev.users.postgres.jdbc-sink-connector"));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
      .contentType(ContentType.JSON)
      .pathParam("path", "dev.users.mssql.jdbc-sink-connector")
      .pathParam("key", "mssql.user")
      .body(createSqlServerUserSecret)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("path", is("dev.users.mssql.jdbc-sink-connector"))
      .body("key", is("mssql.user"))
      .body("version", is(1))
      .body("secret", is(createSqlServerUserSecret.getSecret()));

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
      .pathParam("path", "dev.users.mssql.jdbc-sink-connector")
      .pathParam("key", "mssql.user")
      .pathParam("version", "1")
    .when()
      .delete("/secret/paths/{path}/keys/{key}/versions/{version}")
    .then()
      .statusCode(204);

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
      .pathParam("path", "dev.users.mssql.jdbc-sink-connector")
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
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
      .contentType(ContentType.JSON)
      .pathParam("path", "test-connector")
      .pathParam("key", "tasks.max")
      .body(createTestConnectorSecret)
    .when()
      .post("/secret/paths/{path}/keys/{key}/versions")
    .then()
      .statusCode(201)
      .contentType(ContentType.JSON);

    CreateConnectorDto createTestConnectorDto = CreateConnectorDto.createDummy();

    given()
      .header(HttpHeaders.AUTHORIZATION, Credentials.admin())
      .contentType(ContentType.JSON)
      .body(createTestConnectorDto)
    .when()
      .post("/connectors")
    .then()
      .statusCode(400)
      .contentType(ContentType.JSON)
      .body("message", containsString("Invalid value -1 for configuration tasks.max"));
  }

  @AfterAll
  static void tearDown() {
    CONNECT.stop();
    KAFKA.stop();
    CONFLUENT_CLOUD_API.stop();
  }
}
