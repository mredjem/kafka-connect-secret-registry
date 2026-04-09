package com.github.mredjem.kafka.connect.internals;

import com.github.mredjem.kafka.connect.internals.serdes.KafkaSecretKeySerde;
import com.github.mredjem.kafka.connect.internals.serdes.KafkaSecretValueSerde;
import com.github.mredjem.kafka.connect.internals.utils.HostnameUtils;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Map;
import java.util.Properties;

import static com.github.mredjem.kafka.connect.providers.InternalSecretConfigs.SECRET_REGISTRY_GROUP_ID_CONFIG;

public final class KafkaClients {

  private KafkaClients() {
  }

  public static AdminClient adminClient(Map<String, ?> configs) {
    Properties properties = kafkaProperties(configs);

    return AdminClient.create(properties);
  }

  public static <K, V> Producer<K, V> producer(Map<String, ?> configs) {
    Properties properties = kafkaProperties(configs);

    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, "kafka-connect-secret-registry");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaSecretKeySerde.KafkaSecretKeySerializer.class);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaSecretValueSerde.KafkaSecretValueSerializer.class);
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
    properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
    properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "30000");
    properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "0");

    return new KafkaProducer<>(properties);
  }

  public static <K, V> Consumer<K, V> consumer(Map<String, ?> configs) {
    Properties properties = kafkaProperties(configs);

    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, "kafka-connect-secret-registry");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId(configs));
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaSecretKeySerde.KafkaSecretKeyDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaSecretValueSerde.KafkaSecretValueDeserializer.class);
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE.toString());
    properties.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, Boolean.FALSE.toString());

    return new KafkaConsumer<>(properties);
  }

  private static Properties kafkaProperties(Map<String, ?> configs) {
    Map<String, String> kafkaConfigs = ConfigUtils.getConfigsForPrefix("kafkastore.", configs);

    return ConfigUtils.toProperties(kafkaConfigs);
  }

  private static String groupId(Map<String, ?> configs) {
    String secretRegistryGroupId = (String) configs.get(SECRET_REGISTRY_GROUP_ID_CONFIG);

    return secretRegistryGroupId + "." + HostnameUtils.hostname();
  }
}
