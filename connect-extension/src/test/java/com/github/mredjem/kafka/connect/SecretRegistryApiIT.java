package com.github.mredjem.kafka.connect;

import com.github.mredjem.kafka.connect.extensions.api.SecretRegistryApi;
import com.github.mredjem.kafka.connect.extensions.dtos.CreateSecretDto;
import com.github.mredjem.kafka.connect.extensions.dtos.SecretDto;
import com.github.mredjem.kafka.connect.internals.KafkaInternalTopicRepository;
import com.github.mredjem.kafka.connect.mocks.MockUriInfo;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigProvider;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigs;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import com.github.mredjem.kafka.connect.utils.TestUtils;
import org.apache.kafka.common.config.ConfigData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

class SecretRegistryApiIT extends AbstractIT {

  private static final InternalSecretConfigProvider SECRET_CONFIG_PROVIDER = new InternalSecretConfigProvider();

  private static SecretRegistryPort secretRegistryPort;

  private static SecretRegistryApi secretRegistryApi;

  @BeforeAll
  static void setupSecretRegistry() {
    Map<String, String> configuration = TestUtils.load();

    Map<String, String> extensionConfiguration = ConfigUtils.addEntry(
      ConfigUtils.configsForPrefix("config.providers.secret.param.", configuration),
      "kafkastore.bootstrap.servers",
      "localhost:" + KAFKA.getMappedPort(9092)
    );

    SECRET_CONFIG_PROVIDER.configure(extensionConfiguration);

    secretRegistryPort = KafkaInternalTopicRepository.create(ConfigUtils.addEntry(
      extensionConfiguration,
      InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG,
      extensionConfiguration.get(InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG) + new Random().nextInt(1_000)
    ));

    secretRegistryApi = SecretRegistryApi.create(secretRegistryPort);
  }

  @Test
  void shouldCreateNewSecrets() {
    String path = "dev.users.postgres.jdbc-sink-connector";

    String key = "pg.user";

    CreateSecretDto createPgUserSecret = CreateSecretDto.of("admin");

    Response createdResponse = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createPgUserSecret
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdResponse.getStatus());

    String location = createdResponse.getHeaderString(HttpHeaders.LOCATION);

    Assertions.assertNotNull(location);
    Assertions.assertEquals("http://localhost:8080/secret/paths/" + path + "/keys/" + key + "/versions/1", location);

    Assertions.assertInstanceOf(SecretDto.class, createdResponse.getEntity());

    SecretDto secret = (SecretDto) createdResponse.getEntity();

    Assertions.assertEquals(path, secret.getPath());
    Assertions.assertEquals(key, secret.getKey());
    Assertions.assertEquals(1, secret.getVersion());
    Assertions.assertEquals(createPgUserSecret.getSecret(), secret.getSecret());

    createdResponse.close();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldGetLatestSecretVersion() {
    String path = "dev.users.oracle.jdbc-sink-connector";

    String key = "oracle.user";

    CreateSecretDto createOracleUserV1 = CreateSecretDto.of("admin1");

    Response createdV1Response = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createOracleUserV1
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdV1Response.getStatus());

    createdV1Response.close();

    CreateSecretDto createOracleUserV2 = CreateSecretDto.of("admin2");

    Response createdV2Response = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createOracleUserV2
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdV2Response.getStatus());

    createdV2Response.close();

    Response versionsResponse = secretRegistryApi.listVersionsForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), versionsResponse.getStatus());

    Assertions.assertInstanceOf(List.class, versionsResponse.getEntity());

    List<Integer> versions = (List<Integer>) versionsResponse.getEntity();

    Assertions.assertEquals(2, versions.size());
    Assertions.assertEquals(1, versions.get(0));
    Assertions.assertEquals(2, versions.get(1));

    versionsResponse.close();

    Response latestResponse = secretRegistryApi.getSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions/latest"),
      path,
      key,
      "latest"
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), latestResponse.getStatus());

    Assertions.assertInstanceOf(SecretDto.class, latestResponse.getEntity());

    SecretDto secret = (SecretDto) latestResponse.getEntity();

    Assertions.assertEquals(path, secret.getPath());
    Assertions.assertEquals(key, secret.getKey());
    Assertions.assertEquals(2, secret.getVersion());
    Assertions.assertEquals(createOracleUserV2.getSecret(), secret.getSecret());

    latestResponse.close();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDeleteSecret() {
    String path = "dev.users.mssql.jdbc-sink-connector";

    String key = "mssql.user";

    CreateSecretDto createSqlServerUserSecret = CreateSecretDto.of("admin");

    Response createdResponse = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createSqlServerUserSecret
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdResponse.getStatus());

    createdResponse.close();

    Response deletedResponse = secretRegistryApi.deleteSpecificVersionForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions/1"),
      path,
      key,
      "1"
    );

    Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deletedResponse.getStatus());

    deletedResponse.close();

    Response versionsResponse = secretRegistryApi.listVersionsForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), versionsResponse.getStatus());

    Assertions.assertInstanceOf(List.class, versionsResponse.getEntity());

    List<Integer> versions = (List<Integer>) versionsResponse.getEntity();

    Assertions.assertTrue(versions.isEmpty());

    versionsResponse.close();
  }

  @Test
  void shouldInjectSecretInConnectorConfiguration() throws InterruptedException {
    String path = "test-connector";

    String key = "tasks.max";

    CreateSecretDto createTestConnectorSecret = CreateSecretDto.of("-1");

    Response createdResponse = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createTestConnectorSecret
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdResponse.getStatus());

    createdResponse.close();

    Thread.sleep(1_000L);

    ConfigData configData = SECRET_CONFIG_PROVIDER.get(path, Collections.singleton(key));

    Assertions.assertFalse(configData.data().isEmpty());
    Assertions.assertEquals(createTestConnectorSecret.getSecret(), configData.data().get(key));
  }

  @AfterAll
  static void tearDownSecretRegistry() throws IOException {
    secretRegistryPort.close();

    SECRET_CONFIG_PROVIDER.close();
  }
}
