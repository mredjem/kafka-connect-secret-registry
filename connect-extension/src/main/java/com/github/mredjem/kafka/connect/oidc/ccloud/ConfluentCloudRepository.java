package com.github.mredjem.kafka.connect.oidc.ccloud;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.ResourceName;
import com.github.mredjem.kafka.connect.RoleBinding;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.AccessToken;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.IdentityPoolDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.OwnerDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.dtos.RoleBindingDto;
import com.github.mredjem.kafka.connect.oidc.ccloud.mappers.RoleBindingMapper;
import com.github.mredjem.kafka.connect.oidc.ccloud.utils.ListUtils;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfluentCloudRepository implements OidcPort {

  private final ConfluentCloudClient client;
  private final ResourceName resourceName;

  private ConfluentCloudRepository(Map<String, String> configs) {
    this.client = ConfluentCloudClient.create(configs);
    this.resourceName = ConfluentResourceName.of(ConfigUtils.getOrThrow(OidcConfigs.CLUSTER_CRN_PATTERN_CONFIG, configs));
  }

  public static ConfluentCloudRepository create(Map<String, String> configs) {
    return new ConfluentCloudRepository(configs);
  }

  @Override
  public boolean validateCredentials(AuthenticationCredentials authenticationCredentials) {
    AuthenticationKind kind = authenticationCredentials.getKind();

    return AuthenticationKind.BASIC == kind || AuthenticationKind.BEARER == kind;
  }

  @Override
  public List<RoleBinding> getRoleBindings(AuthenticationCredentials authenticationCredentials) {
    AuthenticationKind kind = authenticationCredentials.getKind();

    if (AuthenticationKind.BASIC == kind) {
      String apiKeyId = this.getAPIKeyId(authenticationCredentials);

      return this.getRoleBindingsForAPIKey(apiKeyId);
    }

    return this.getRoleBindingsForExternalAccessToken(authenticationCredentials.getCredentials());
  }

  private List<RoleBinding> getRoleBindingsForAPIKey(String apiKeyId) {
    OwnerDto serviceAccount = this.client.readAPIKey(apiKeyId).getSpec().getOwner();

    CompletableFuture<List<RoleBindingDto>> organizationCf = CompletableFuture.supplyAsync(() -> this.client.listRoleBindings(this.resourceName.organizationUrn() + "/*", serviceAccount.getId()));
    CompletableFuture<List<RoleBindingDto>> environmentCf  = CompletableFuture.supplyAsync(() -> this.client.listRoleBindings(this.resourceName.environmentUrn() + "/*", serviceAccount.getId()));
    CompletableFuture<List<RoleBindingDto>> cloudClusterCf = CompletableFuture.supplyAsync(() -> this.client.listRoleBindings(this.resourceName.clusterUrn() + "/connector=*", serviceAccount.getId()));

    return CompletableFuture.allOf(organizationCf, environmentCf, cloudClusterCf)
      .thenApplyAsync(aVoid -> ListUtils.merge(organizationCf.join(), environmentCf.join(), cloudClusterCf.join()))
      .join()
      .stream()
      .map(RoleBindingMapper::map)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private List<RoleBinding> getRoleBindingsForExternalAccessToken(String externalAccessToken) {
    Map<String, Object> claims = AccessToken.parse(externalAccessToken).getClaims();

    Predicate<IdentityPoolDto> identityPoolPredicate = this.identityPoolPredicate(claims);

    CompletableFuture<List<RoleBindingDto>> organizationCf = CompletableFuture.supplyAsync(() -> this.client.listRoleBindings(this.resourceName.organizationUrn() + "/*", identityPoolPredicate));
    CompletableFuture<List<RoleBindingDto>> environmentCf  = CompletableFuture.supplyAsync(() -> this.client.listRoleBindings(this.resourceName.environmentUrn() + "/*", identityPoolPredicate));
    CompletableFuture<List<RoleBindingDto>> cloudClusterCf = CompletableFuture.supplyAsync(() -> this.client.listRoleBindings(this.resourceName.clusterUrn() + "/connector=*", identityPoolPredicate));

    return CompletableFuture.allOf(organizationCf, environmentCf, cloudClusterCf)
      .thenApplyAsync(aVoid -> ListUtils.merge(organizationCf.join(), environmentCf.join(), cloudClusterCf.join()))
      .join()
      .stream()
      .map(RoleBindingMapper::map)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private Predicate<IdentityPoolDto> identityPoolPredicate(Map<String, Object> claims) {
    return identityPool -> {
      CelFilter celFilter = CelFilter.parse(identityPool.getFilter());

      return celFilter.evaluate(claims);
    };
  }

  private String getAPIKeyId(AuthenticationCredentials authenticationCredentials) {
    String decodedCredentials = new String(Base64.getDecoder().decode(authenticationCredentials.getCredentials()));

    String[] usernameAndPassword = decodedCredentials.split(":");

    if (usernameAndPassword.length != 2) {
      throw new IllegalArgumentException("Invalid basic credentials");
    }

    return usernameAndPassword[0];
  }
}
