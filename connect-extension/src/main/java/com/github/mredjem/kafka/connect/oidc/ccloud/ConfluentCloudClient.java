package com.github.mredjem.kafka.connect.oidc.ccloud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mredjem.kafka.connect.oidc.HttpClient;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.ApiKeyDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.DataResponseDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.IdentityPoolDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.IdentityProviderDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.RoleBindingDto;
import com.github.mredjem.kafka.connect.oidc.exceptions.ResourceNotFoundException;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConfluentCloudClient {

  private final HttpClient httpClient;

  private final AsyncCache<List<ApiKeyDto>> apiKeysCache;
  private final AsyncCache<List<IdentityPoolDto>> identityPoolsCache;

  private ConfluentCloudClient(Map<String, String> configs) {
    this.httpClient = HttpClient.create(
      configs.getOrDefault(OidcConfigs.API_BASE_URL_CONFIG, "https://api.confluent.cloud"),
      ConfigUtils.getOrThrow(OidcConfigs.API_KEY_CONFIG, configs),
      ConfigUtils.getOrThrow(OidcConfigs.API_SECRET_CONFIG, configs)
    );

    this.apiKeysCache = AsyncCache.create(this::listApiKeys);

    this.identityPoolsCache = AsyncCache.create(() -> {
      String identityProviderName = ConfigUtils.getOrThrow(OidcConfigs.IDENTITY_PROVIDER_NAME_CONFIG, configs);

      return this.listIdentityPools(identityProviderName);
    });
  }

  public static ConfluentCloudClient create(Map<String, String> configs) {
    ConfluentCloudClient confluentCloudClient = new ConfluentCloudClient(configs);

    confluentCloudClient.apiKeysCache.init();
    confluentCloudClient.identityPoolsCache.init();

    return confluentCloudClient;
  }

  public List<RoleBindingDto> listRoleBindings(String crnPattern, String principal) {
    String path = UriBuilder.fromPath("iam/v2/role-bindings")
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
    return this.apiKeysCache.getAll()
      .stream()
      .filter(apiKey -> apiKey.getId().equals(apiKeyId))
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException(apiKeyId));
  }

  public List<ApiKeyDto> listApiKeys() {
    DataResponseDto<ApiKeyDto> data = this.httpClient.doGET("iam/v2/api-keys", new TypeReference<DataResponseDto<ApiKeyDto>>() {});

    return data.getData();
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

    String path = UriBuilder.fromPath("iam/v2/identity-providers/{identityProvider}/identity-pools")
      .build(identityProvider.getId())
      .toString();

    return this.httpClient.doGET(path, new TypeReference<DataResponseDto<IdentityPoolDto>>() {}).getData();
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
    DataResponseDto<IdentityProviderDto> data = this.httpClient.doGET("iam/v2/identity-providers", new TypeReference<DataResponseDto<IdentityProviderDto>>() {});

    return data.getData();
  }
}
