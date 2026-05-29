package com.github.mredjem.kafka.connect.oidc.ccloud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.oidc.HttpClient;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.ApiKeyDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.DataResponseDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.IdentityPoolDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.IdentityProviderDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.OAuthTokenDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.RoleBindingDto;
import com.github.mredjem.kafka.connect.oidc.exceptions.ResourceNotFoundException;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class ConfluentCloudClient {

  private static final String CONFLUENT_CLOUD_API = "https://confluent.cloud";

  private final HttpClient httpClient;

  private final AsyncCache<ApiKeyDto> apiKeysCache;
  private final AsyncCache<IdentityPoolDto> identityPoolsCache;

  private ConfluentCloudClient(Map<String, String> configs) {
    AuthenticationCredentials basicCredentials = AuthenticationCredentials.of(
      ConfigUtils.getOrThrow(OidcConfigs.API_KEY_CONFIG, configs),
      ConfigUtils.getOrThrow(OidcConfigs.API_SECRET_CONFIG, configs)
    );

    this.httpClient = HttpClient.create(
      configs.getOrDefault(OidcConfigs.API_BASE_URL_CONFIG, CONFLUENT_CLOUD_API),
      basicCredentials
    );

    this.apiKeysCache = AsyncCache.create(this::listApiKeys);

    this.identityPoolsCache = AsyncCache.create(() -> {
      String identityProviderName = ConfigUtils.getOrThrow(OidcConfigs.IDENTITY_PROVIDER_NAME_CONFIG, configs);

      return this.listIdentityPools(identityProviderName);
    });
  }

  public static ConfluentCloudClient create(Map<String, String> configs) {
    return new ConfluentCloudClient(configs);
  }

  public List<String> listConnectors(String environmentId, String clusterId, AuthenticationCredentials authenticationCredentials) {
    HttpClient connectHttpClient = HttpClient.create(this.httpClient.getBaseUrl(), authenticationCredentials);

    String path = UriBuilder.fromPath("api/connect/v1/environments/{environmentId}/clusters/{clusterId}/connectors")
      .build(environmentId, clusterId)
      .toString();

    return connectHttpClient.doGET(path, new TypeReference<List<String>>() {});
  }

  public List<String> listConnectors(String environmentId, String clusterId, AuthenticationCredentials authenticationCredentials, Predicate<IdentityPoolDto> identityPoolPredicate) {
    IdentityPoolDto identityPool = this.readIdentityPool(identityPoolPredicate);

    OAuthTokenDto oAuthToken = this.exchangeOAuthToken(identityPool.getId(), authenticationCredentials);

    HttpClient connectHttpClient = HttpClient.create(this.httpClient.getBaseUrl(), AuthenticationCredentials.of(oAuthToken.getAccessToken()));

    String path = UriBuilder.fromPath("api/connect/v1/environments/{environmentId}/clusters/{clusterId}/connectors")
      .build(environmentId, clusterId)
      .toString();

    return connectHttpClient.doGET(path, new TypeReference<List<String>>() {});
  }

  public List<RoleBindingDto> listRoleBindings(String crnPattern, String principal) {
    String path = UriBuilder.fromPath("api/iam/v2/role-bindings")
      .queryParam("principal", "User:" + principal)
      .queryParam("crn_pattern", crnPattern)
      .build()
      .toString();

    return this.httpClient.doGET(path, new TypeReference<DataResponseDto<RoleBindingDto>>() {}).getData();
  }

  public List<RoleBindingDto> listRoleBindings(String crnPattern, Predicate<IdentityPoolDto> identityPoolPredicate) {
    IdentityPoolDto appIdentityPool = this.readIdentityPool(identityPoolPredicate);

    return this.listRoleBindings(crnPattern, appIdentityPool.getId());
  }

  public ApiKeyDto readAPIKey(String apiKeyId) {
    Optional<ApiKeyDto> cachedApiKey = this.apiKeysCache.getAll()
      .stream()
      .filter(apiKey -> apiKey.getId().equals(apiKeyId))
      .findFirst();

    if (cachedApiKey.isPresent()) {
      return cachedApiKey.get();
    }

    String path = UriBuilder.fromPath("api/iam/v2/api-keys/{apiKeyId}")
      .build(apiKeyId)
      .toString();

    return this.httpClient.doGET(path, new TypeReference<ApiKeyDto>() {});
  }

  public List<ApiKeyDto> listApiKeys() {
    return this.listAll("api/iam/v2/api-keys", new TypeReference<DataResponseDto<ApiKeyDto>>() {});
  }

  private IdentityPoolDto readIdentityPool(Predicate<IdentityPoolDto> identityPoolDtoPredicate) {
    return this.identityPoolsCache.getAll()
      .stream()
      .filter(identityPool -> "ENABLED".equalsIgnoreCase(identityPool.getState()))
      .filter(identityPoolDtoPredicate)
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException("Identity pool"));
  }

  private List<IdentityPoolDto> listIdentityPools(String identityProviderName) {
    IdentityProviderDto identityProvider = this.readIdentityProvider(identityProviderName);

    String path = UriBuilder.fromPath("api/iam/v2/identity-providers/{identityProvider}/identity-pools")
      .build(identityProvider.getId())
      .toString();

    return this.listAll(path, new TypeReference<DataResponseDto<IdentityPoolDto>>() {});
  }

  private IdentityProviderDto readIdentityProvider(String identityProviderName) {
    return this.listIdentityProviders()
      .stream()
      .filter(identityProvider -> "ENABLED".equalsIgnoreCase(identityProvider.getState())
        && "OK".equalsIgnoreCase(identityProvider.getJwksStatus())
        && identityProvider.getDisplayName().equalsIgnoreCase(identityProviderName)
      )
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException("Identity provider", identityProviderName));
  }

  private List<IdentityProviderDto> listIdentityProviders() {
    DataResponseDto<IdentityProviderDto> data = this.httpClient.doGET("api/iam/v2/identity-providers", new TypeReference<DataResponseDto<IdentityProviderDto>>() {});

    return data.getData();
  }

  private OAuthTokenDto exchangeOAuthToken(String identityPoolId, AuthenticationCredentials authenticationCredentials) {
    Map<String, String> parameters = new HashMap<>();

    parameters.put("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
    parameters.put("subject_token", authenticationCredentials.getCredentials());
    parameters.put("subject_token_type", "urn:ietf:params:oauth:token-type:jwt");
    parameters.put("requested_token_type", "urn:ietf:params:oauth:token-type:access_token");
    parameters.put("identity_pool_id", identityPoolId);

    return this.httpClient.doPOST("api/sts/v1/oauth2/token", parameters, new TypeReference<OAuthTokenDto>() {});
  }

  private <T> List<T> listAll(String initialPath, TypeReference<DataResponseDto<T>> typeReference) {
    DataResponseDto<T> initialDataResponse = this.httpClient.doGET(initialPath + "?page_size=100", typeReference);

    List<T> results = new ArrayList<>(initialDataResponse.getData());

    String previous = normalizePath(initialDataResponse.getMetadata().getFirst());
    String next     = normalizePath(initialDataResponse.getMetadata().getNext());

    int iteration = 0;

    while (next != null && !next.equals(previous) && ++iteration <= 10) {
      String path = next + "&page_size=100";

      DataResponseDto<T> dataResponse = this.httpClient.doGET(path, typeReference);

      results.addAll(dataResponse.getData());

      previous = normalizePath(next);
      next     = normalizePath(dataResponse.getMetadata().getNext());
    }

    return results;
  }

  private static String normalizePath(String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    return path.replace(CONFLUENT_CLOUD_API + "/", "");
  }
}
