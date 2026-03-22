# Kafka Connect Secret Registry Extension

This is a REST extension for Kafka Connect allowing to store connector secrets in an internal Kafka topic.

Secrets can be registered by issuing REST requests to the additional Kafka Connect endpoints provider by this extension.

Connectors can reference those secrets in their configuration without exposing credentials to end users.

> This extension works pretty much in the same way as the [Connect Secret Registry for Confluent Platform](https://docs.confluent.io/platform/current/connect/rbac/connect-rbac-secret-registry.html) but does not require the Confluent Metadata Service.

## Installation

1. Place the JAR file in the plugins directory of each Kafka Connect worker.
2. Add the `SecretRegistryExtension` extension class in the Kafka Connect worker properties:

```properties
rest.extension.classes=com.github.mredjem.kafka.connect.extensions.SecretRegistryExtension
```

3. Add the `InternalSecretConfigProvider` configuration provider class in the Kafka Connect worker properties:

```properties
config.providers=secret
config.providers.secret.class=com.github.mredjem.kafka.connect.providers.InternalSecretConfigProvider
config.providers.secret.param.kafkastore.bootstrap.servers=localhost:9092
config.providers.secret.param.kafkastore.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="<username>" password="<password>";
config.providers.secret.param.kafkastore.sasl.mechanism=PLAIN
config.providers.secret.param.kafkastore.security.protocol=SASL_SSL
config.providers.secret.param.kafkastore.topic=_connect-secrets
config.providers.secret.param.kafkastore.topic.replication.factor=1
config.providers.secret.param.master.encryption.key=<encryption key>
config.providers.secret.param.secret.registry.group.id=secret-registry
config.providers.secret.param.super.admins=admin:password,centreon:password:read
```

## REST endpoints

**List all paths**

```
GET /secret/paths
```

**Get all latest secret versions under path**

```
GET /secret/paths/{path}
```

**List keys under path**

```
GET /secret/paths/{path}/keys
```

**Get all secret versions for key**

```
GET /secret/paths/{path}/keys/{key}
```

**List all versions of key**

```
GET /secret/paths/{path}/keys/{key}/versions
```

**Get a specific version of a key**

```
GET /secret/paths/{path}/keys/{key}/versions/{version}
```

**Get a secret**

```
GET /secret/paths/{path}/keys/{key}/versions/latest
```

**Create a secret**

```
POST /secret/paths/{path}/keys/{key}/versions
{
  "secret": "a secret"
}
```

**Delete a specific version of a key**

```
DELETE /secret/paths/{path}/keys/{key}/versions/{version}
```

**Delete all versions of a key**

```
DELETE /secret/paths/{path}/keys/{key}
```

**Delete a path**

```
DELETE /secret/paths/{path}
```
