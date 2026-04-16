package com.github.mredjem.kafka.connect.oidc.azure.ccloud;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.github.mredjem.kafka.connect.AuthenticationKind;
import com.github.mredjem.kafka.connect.ResourceName;
import com.github.mredjem.kafka.connect.RoleBinding;
import com.github.mredjem.kafka.connect.oidc.OidcConfigs;
import com.github.mredjem.kafka.connect.oidc.OidcPort;
import com.github.mredjem.kafka.connect.oidc.azure.EntraIDToken;
import com.github.mredjem.kafka.connect.oidc.azure.ccloud.dtos.IdentityPoolDto;
import com.github.mredjem.kafka.connect.oidc.azure.ccloud.dtos.OwnerDto;
import com.github.mredjem.kafka.connect.oidc.azure.ccloud.mappers.RoleBindingMapper;
import com.github.mredjem.kafka.connect.utils.ConfigUtils;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
      String decodedCredentials = new String(Base64.getDecoder().decode(authenticationCredentials.getCredentials()));

      return this.getRoleBindingsForAPIKey(decodedCredentials.split(":")[0]);
    }

    return this.getRoleBindingsForExternalAccessToken(authenticationCredentials.getCredentials());
  }

  private List<RoleBinding> getRoleBindingsForAPIKey(String apiKeyId) {
    OwnerDto serviceAccount = this.client.readAPIKey(apiKeyId).getSpec().getOwner();

    return Stream.of(
        this.client.listRoleBindings(this.resourceName.organizationUrn() + "/*", serviceAccount.getId()),
        this.client.listRoleBindings(this.resourceName.environmentUrn() + "/*", serviceAccount.getId()),
        this.client.listRoleBindings(this.resourceName.clusterUrn() + "/connector=*", serviceAccount.getId())
      )
      .flatMap(Collection::stream)
      .map(RoleBindingMapper::map)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private List<RoleBinding> getRoleBindingsForExternalAccessToken(String externalAccessToken) {
    Map<String, Object> claims = EntraIDToken.parse(externalAccessToken).getClaims();

    Predicate<IdentityPoolDto> identityPoolPredicate = this.identityPoolPredicate(claims);

    return Stream.of(
        this.client.listRoleBindings(this.resourceName.organizationUrn() + "/*", identityPoolPredicate),
        this.client.listRoleBindings(this.resourceName.environmentUrn() + "/*", identityPoolPredicate),
        this.client.listRoleBindings(this.resourceName.clusterUrn() + "/connector=*", identityPoolPredicate)
      )
      .flatMap(Collection::stream)
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
}
