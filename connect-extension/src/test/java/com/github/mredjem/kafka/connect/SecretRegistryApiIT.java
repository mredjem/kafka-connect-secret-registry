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
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    String path = "tst.users.postgres.jdbc-sink-connector";

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
    String path = "tst.users.oracle.jdbc-sink-connector";

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

    Awaitility.await().atMost(1L, TimeUnit.SECONDS).until(() -> {
      Response version2Response = secretRegistryApi.getSecret(
        MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions/2"),
        path,
        key,
        "2"
      );

      return Response.Status.OK.getStatusCode() == version2Response.getStatus();
    });

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
  void shouldListSecretsUnderPathOrKey() {
    String path = "tst.azure.storage.blob-sink-connector";

    String key = "storage.account";

    CreateSecretDto createStorageAccountSecretV1 = CreateSecretDto.of("app1");

    Response createdV1Response = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createStorageAccountSecretV1
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdV1Response.getStatus());

    createdV1Response.close();

    CreateSecretDto createStorageAccountSecretV2 = CreateSecretDto.of("app2");

    Response createdV2Response = secretRegistryApi.createSecret(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key,
      createStorageAccountSecretV2
    );

    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdV2Response.getStatus());

    createdV2Response.close();

    // check available paths
    Response pathsResponse = secretRegistryApi.listAllPaths(MockUriInfo.of("/secret/paths"));

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), pathsResponse.getStatus());
    Assertions.assertInstanceOf(List.class, pathsResponse.getEntity());

    List<String> paths = (List<String>) pathsResponse.getEntity();

    Assertions.assertTrue(paths.contains(path));

    pathsResponse.close();

    // check available keys under path
    Response keysResponse = secretRegistryApi.listAllKeysForPath(
      MockUriInfo.of("/secret/paths/" + path + "/keys"),
      path
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), keysResponse.getStatus());
    Assertions.assertInstanceOf(List.class, keysResponse.getEntity());

    List<String> keys = (List<String>) keysResponse.getEntity();

    Assertions.assertTrue(keys.contains(key));

    keysResponse.close();

    // check available secrets under path
    Response pathSecretsResponse = secretRegistryApi.getAllLatestVersionsForKeysInPath(
      MockUriInfo.of("/secret/paths/" + path),
      path
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), pathSecretsResponse.getStatus());
    Assertions.assertInstanceOf(List.class, pathSecretsResponse.getEntity());

    Map<String, SecretDto> secretsPerKey = ((List<SecretDto>) pathSecretsResponse.getEntity())
      .stream()
      .collect(Collectors.toMap(
        SecretDto::getKey,
        Function.identity()
    ));

    Assertions.assertTrue(secretsPerKey.containsKey(key));
    Assertions.assertEquals(path, secretsPerKey.get(key).getPath());
    Assertions.assertEquals(2, secretsPerKey.get(key).getVersion());
    Assertions.assertEquals("app2", secretsPerKey.get(key).getSecret());

    pathSecretsResponse.close();

    // check available secrets under key
    Response keySecretsResponse = secretRegistryApi.getAllVersionsForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key),
      path,
      key
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), keySecretsResponse.getStatus());
    Assertions.assertInstanceOf(List.class, keySecretsResponse.getEntity());

    Map<Integer, SecretDto> secretsPerVersion = ((List<SecretDto>) keySecretsResponse.getEntity())
      .stream()
      .collect(Collectors.toMap(
        SecretDto::getVersion,
        Function.identity()
      ));

    Assertions.assertTrue(secretsPerVersion.containsKey(1));
    Assertions.assertTrue(secretsPerVersion.containsKey(2));

    Assertions.assertEquals("app1", secretsPerVersion.get(1).getSecret());
    Assertions.assertEquals("app2", secretsPerVersion.get(2).getSecret());

    keySecretsResponse.close();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDeleteSecret() {
    String path = "tst.users.mssql.jdbc-sink-connector";

    String key = "mssql.user";

    for (int i = 1; i <= 3; i++) {
      CreateSecretDto createSqlServerUserSecret = CreateSecretDto.of("admin" + i);

      Response createdResponse = secretRegistryApi.createSecret(
        MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
        path,
        key,
        createSqlServerUserSecret
      );

      Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createdResponse.getStatus());

      createdResponse.close();
    }

    // delete version 1
    Response deletedResponse = secretRegistryApi.deleteSpecificVersionForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions/1"),
      path,
      key,
      "1"
    );

    Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deletedResponse.getStatus());

    deletedResponse.close();

    Awaitility.await().atMost(1L, TimeUnit.SECONDS).until(() -> {
      Response version1Response = secretRegistryApi.getSecret(
        MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions/1"),
        path,
        key,
        "1"
      );

      return Response.Status.NOT_FOUND.getStatusCode() == version1Response.getStatus();
    });

    // list available versions under key
    Response versionsResponse = secretRegistryApi.listVersionsForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions"),
      path,
      key
    );

    Assertions.assertEquals(Response.Status.OK.getStatusCode(), versionsResponse.getStatus());
    Assertions.assertInstanceOf(List.class, versionsResponse.getEntity());

    List<Integer> versions = (List<Integer>) versionsResponse.getEntity();

    Assertions.assertFalse(versions.contains(1));
    Assertions.assertTrue(versions.contains(2));
    Assertions.assertTrue(versions.contains(3));

    versionsResponse.close();

    // delete all secrets under key
    Response keysRemovalResponse = secretRegistryApi.deleteAllVersionsForKey(
      MockUriInfo.of("/secret/paths/" + path + "/keys/" + key),
      path,
      key
    );

    Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), keysRemovalResponse.getStatus());

    keysRemovalResponse.close();

    // delete path
    Response pathRemovalResponse = secretRegistryApi.deletePath(
      MockUriInfo.of("/secret/paths/" + path),
      path
    );

    Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), pathRemovalResponse.getStatus());

    pathRemovalResponse.close();
  }

  @Test
  void shouldInjectSecretInConnectorConfiguration() {
    String path = "tst-connector";

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

    Awaitility.await().atMost(1L, TimeUnit.SECONDS).until(() -> {
      Response latestResponse = secretRegistryApi.getSecret(
        MockUriInfo.of("/secret/paths/" + path + "/keys/" + key + "/versions/latest"),
        path,
        key,
        "latest"
      );

      return Response.Status.OK.getStatusCode() == latestResponse.getStatus();
    });

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
