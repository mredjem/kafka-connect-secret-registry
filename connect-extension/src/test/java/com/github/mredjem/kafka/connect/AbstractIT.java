package com.github.mredjem.kafka.connect;

import com.github.mredjem.kafka.connect.containers.ConfluentKafkaConnectContainer;
import com.github.mredjem.kafka.connect.extensions.SecretRegistryExtension;
import com.github.mredjem.kafka.connect.mocks.ConfluentCloudApi;
import com.github.mredjem.kafka.connect.providers.InternalSecretConfigProvider;
import com.github.mredjem.kafka.connect.utils.SocketUtils;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.util.concurrent.TimeUnit;

@Testcontainers
abstract class AbstractIT {

  protected static final int MOCKSERVER_PORT = SocketUtils.nextAvailablePort();

  static {
    // must be called before containers are started
    org.testcontainers.Testcontainers.exposeHostPorts(MOCKSERVER_PORT);
  }

  protected static final Network NETWORK = Network.newNetwork();

  @Container
  protected static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.0")
    .withNetwork(NETWORK)
    .withNetworkAliases("kafka")
    .withExposedPorts(9092, 9093, 29092)
    .withListener("kafka:29092")
    .withEnv("KAFKA_NODE_ID", "1")
    .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false")
    .withEnv("CONFLUENT_METRICS_ENABLE", "false");

  @Container
  protected static final ConfluentKafkaConnectContainer CONNECT = new ConfluentKafkaConnectContainer("confluentinc/cp-kafka-connect-base:7.7.0")
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

  protected static final ConfluentCloudApi CONFLUENT_CLOUD_API = ConfluentCloudApi.create();

  @BeforeAll
  static void setup() {
    CONFLUENT_CLOUD_API.start(MOCKSERVER_PORT);

    Awaitility.await().atMost(5L, TimeUnit.SECONDS).until(CONFLUENT_CLOUD_API::isRunning);

    RestAssured.baseURI = "http://localhost:" + CONNECT.getMappedPort(8083);
  }

  @BeforeEach
  void beforeEach() {
    CONFLUENT_CLOUD_API.initMocks();
  }

  @AfterAll
  static void tearDown() {
    CONFLUENT_CLOUD_API.stop();
  }
}
