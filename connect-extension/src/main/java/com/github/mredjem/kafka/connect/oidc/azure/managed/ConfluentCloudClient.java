package com.github.mredjem.kafka.connect.oidc.azure.managed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.azure.managed.dtos.DataResponseDto;
import com.github.mredjem.kafka.connect.oidc.azure.managed.dtos.IdentityPoolDto;
import com.github.mredjem.kafka.connect.oidc.azure.managed.dtos.IdentityProviderDto;
import com.github.mredjem.kafka.connect.oidc.azure.managed.dtos.RoleBindingDto;
import com.github.mredjem.kafka.connect.oidc.exceptions.ResourceNotFoundException;
import com.github.mredjem.kafka.connect.oidc.utils.JwtUtils;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ConfluentCloudClient {

  private static final ObjectMapper OM = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private final String apiBaseUrl;

  private final String organization;

  private final String idpName;

  private final String credentials;

  private ConfluentCloudClient(Map<String, String> configs) {
    this.apiBaseUrl = configs.getOrDefault(OidcConfigs.API_BASE_URL_CONFIG, "https://api.confluent.cloud");
    this.organization = ConfigUtils.getOrThrow(OidcConfigs.ORGANIZATION_ID_CONFIG, configs);
    this.idpName = ConfigUtils.getOrThrow(OidcConfigs.IDENTITY_PROVIDER_NAME_CONFIG, configs);
    this.credentials = JwtUtils.encode(configs, OidcConfigs.API_KEY_CONFIG, OidcConfigs.API_SECRET_CONFIG);
  }

  public static ConfluentCloudClient create(Map<String, String> configs) {
    return new ConfluentCloudClient(configs);
  }

  public List<RoleBindingDto> listRoleBindings(AuthenticationCredentials authenticationCredentials) {
    Map<String, Object> parsedPayload = JwtUtils.checkIssuer(JwtUtils.parsePayload(authenticationCredentials.getCredentials()), this.organization);

    IdentityPoolDto appIdentityPool = this.listIdentityPools()
      .stream()
      .filter(identityPool -> JwtUtils.doesIdentityMatch(parsedPayload, identityPool.getIdentityClaim(), identityPool.getFilter()))
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException("Identity pool"));

    String path = "iam/v2/role-bindings?principal=User:" + appIdentityPool.getId() + "&crn_pattern=crn://confluent.cloud/organization=" + this.organization + "/*";

    return this.doGET(path, new TypeReference<DataResponseDto<RoleBindingDto>>() {}).getData();
  }

  private List<IdentityPoolDto> listIdentityPools() {
    IdentityProviderDto azureIdentityProvider = this.listIdentityProviders()
      .stream()
      .filter(identityProvider -> "ENABLED".equalsIgnoreCase(identityProvider.getState()) && "OK".equalsIgnoreCase(identityProvider.getJwksStatus()) && identityProvider.getDisplayName().equalsIgnoreCase(this.idpName))
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException("Identity provider", this.idpName));

    String path = "iam/v2/identity-providers/" + azureIdentityProvider.getId() + "/identity-pools";

    return this.doGET(path, new TypeReference<DataResponseDto<IdentityPoolDto>>() {}).getData();
  }

  private List<IdentityProviderDto> listIdentityProviders() {
    DataResponseDto<IdentityProviderDto> data = this.doGET("iam/v2/identity-providers", new TypeReference<DataResponseDto<IdentityProviderDto>>() {});

    return data.getData();
  }

  private <T> T doGET(String path, TypeReference<T> typeReference) {
    BufferedReader br = null;

    try {
      URL url = new URL(String.format("%s/%s", this.apiBaseUrl, path));

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(HttpMethod.GET);
      connection.setRequestProperty(HttpHeaders.AUTHORIZATION, "Basic " + this.credentials);
      connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
      connection.setConnectTimeout(5_000);
      connection.setReadTimeout(5_000);
      connection.setDoOutput(true);

      br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      StringBuilder response = new StringBuilder();

      String line;

      while ((line = br.readLine()) != null) {
        response.append(line);
      }

      return OM.readValue(response.toString(), typeReference);

    } catch (final IOException e) {
      throw new UncheckedIOException(e);

    } finally {
      if (br != null) {
        try {
          br.close();

        } catch (final IOException ignored) {
          // noop
        }
      }
    }
  }
}
