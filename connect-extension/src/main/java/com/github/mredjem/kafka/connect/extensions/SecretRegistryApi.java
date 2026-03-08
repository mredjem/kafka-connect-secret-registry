package com.github.mredjem.kafka.connect.extensions;

import com.github.mredjem.kafka.connect.Key;
import com.github.mredjem.kafka.connect.Secret;
import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.Version;
import com.github.mredjem.kafka.connect.extensions.dtos.CreateSecretDto;
import com.github.mredjem.kafka.connect.extensions.dtos.SecretDto;
import com.github.mredjem.kafka.connect.extensions.exceptions.ResourceNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("secret")
public class SecretRegistryApi {

  private final SecretRegistryPort secretRegistryPort;

  private SecretRegistryApi(SecretRegistryPort secretRegistryPort) {
    this.secretRegistryPort = secretRegistryPort;
  }

  public static SecretRegistryApi create(SecretRegistryPort secretRegistryPort) {
    return new SecretRegistryApi(secretRegistryPort);
  }

  @GET
  @Path("paths")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> listAllPaths() {
    return this.secretRegistryPort.getPaths()
      .stream()
      .map(com.github.mredjem.kafka.connect.Path::getPath)
      .sorted(Comparator.naturalOrder())
      .collect(Collectors.toList());
  }

  @GET
  @Path("paths/{path}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SecretDto> getAllLatestVersionsForKeysInPath(@PathParam("path") String path) {
    Set<String> keys = this.secretRegistryPort.getKeys(path)
      .stream()
      .map(Key::getKey)
      .collect(Collectors.toSet());

    return this.secretRegistryPort.getSecrets(path, keys)
      .stream()
      .map(SecretDto::toDto)
      .sorted(Comparator.comparing(SecretDto::getKey))
      .collect(Collectors.toList());
  }

  @GET
  @Path("paths/{path}/keys")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> listAllKeysForPath(@PathParam("path") String path) {
    return this.secretRegistryPort.getKeys(path)
      .stream()
      .map(Key::getKey)
      .sorted(Comparator.naturalOrder())
      .collect(Collectors.toList());
  }

  @GET
  @Path("paths/{path}/keys/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SecretDto> getAllVersionsForKey(@PathParam("path") String path, @PathParam("key") String key) {
    return this.secretRegistryPort.getSecrets(path, key)
      .stream()
      .map(SecretDto::toDto)
      .sorted(Comparator.comparing(SecretDto::getVersion))
      .collect(Collectors.toList());
  }

  @GET
  @Path("paths/{path}/keys/{key}/versions")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Integer> listVersionsForKey(@PathParam("path") String path, @PathParam("key") String key) {
    return this.secretRegistryPort.getVersions(path, key)
      .stream()
      .map(Version::getVersion)
      .sorted(Comparator.naturalOrder())
      .collect(Collectors.toList());
  }

  @GET
  @Path("paths/{path}/keys/{key}/versions/{version}")
  @Produces(MediaType.APPLICATION_JSON)
  public SecretDto getSecret(
    @PathParam("path") String path,
    @PathParam("key") String key,
    @PathParam("version") String version
  ) {
    return this.secretRegistryPort.getSecret(path, key, version)
      .map(SecretDto::toDto)
      .orElseThrow(() -> new ResourceNotFoundException("secret", path + "/" + key + "/" + version));
  }

  @POST
  @Path("paths/{path}/keys/{key}/versions")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createSecret(
    @Context UriInfo uriInfo,
    @PathParam("path") String path,
    @PathParam("key") String key,
    CreateSecretDto createSecretDto
  ) {
    Secret createdSecret = this.secretRegistryPort.createSecret(path, key, createSecretDto.getSecret());

    URI resourceUri = uriInfo.getAbsolutePathBuilder()
      .path(String.valueOf(createdSecret.getVersion().getVersion()))
      .build();

    return Response.created(resourceUri)
      .entity(SecretDto.toDto(createdSecret))
      .build();
  }


  @DELETE
  @Path("paths/{path}/keys/{key}/versions/{version}")
  public Response deleteSpecificVersionForKey(
    @PathParam("path") String path,
    @PathParam("key") String key,
    @PathParam("version") String version
  ) {
    this.secretRegistryPort.deleteSecret(path, key, version);

    return Response.noContent().build();
  }

  @DELETE
  @Path("paths/{path}/keys/{key}")
  public Response deleteAllVersionsForKey(@PathParam("path") String path, @PathParam("key") String key) {
    this.secretRegistryPort.deleteKey(path, key);

    return Response.noContent().build();
  }

  @DELETE
  @Path("paths/{path}")
  public Response deletePath(@PathParam("path") String path) {
    this.secretRegistryPort.deletePath(path);

    return Response.noContent().build();
  }
}
