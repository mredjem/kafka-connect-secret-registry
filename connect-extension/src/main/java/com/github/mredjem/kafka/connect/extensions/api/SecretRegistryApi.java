package com.github.mredjem.kafka.connect.extensions.api;

import com.github.mredjem.kafka.connect.Key;
import com.github.mredjem.kafka.connect.Secret;
import com.github.mredjem.kafka.connect.SecretRegistryPort;
import com.github.mredjem.kafka.connect.Version;
import com.github.mredjem.kafka.connect.extensions.dtos.CreateSecretDto;
import com.github.mredjem.kafka.connect.extensions.dtos.SecretDto;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Path("secret")
@RequiredArgsConstructor(staticName = "create")
public class SecretRegistryApi {

  private final SecretRegistryPort secretRegistryPort;

  @GET
  @Path("paths")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listAllPaths(@Context UriInfo uriInfo) {
    Supplier<Response> supplier = () -> {
      List<String> paths = this.secretRegistryPort.getPaths()
        .stream()
        .map(com.github.mredjem.kafka.connect.Path::getValue)
        .sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());

      return Response.ok().entity(paths).build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @GET
  @Path("paths/{path}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllLatestVersionsForKeysInPath(@Context UriInfo uriInfo, @PathParam("path") String path) {
    Supplier<Response> supplier = () -> {
      Set<String> keys = this.secretRegistryPort.getKeys(path)
        .stream()
        .map(Key::getValue)
        .collect(Collectors.toSet());

      List<SecretDto> secrets = this.secretRegistryPort.getSecrets(path, keys)
        .stream()
        .map(SecretDto::toDto)
        .sorted(Comparator.comparing(SecretDto::getKey))
        .collect(Collectors.toList());

      return Response.ok().entity(secrets).build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @GET
  @Path("paths/{path}/keys")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listAllKeysForPath(@Context UriInfo uriInfo, @PathParam("path") String path) {
    Supplier<Response> supplier = () -> {
      List<String> keys = this.secretRegistryPort.getKeys(path)
        .stream()
        .map(Key::getValue)
        .sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());

      return Response.ok().entity(keys).build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @GET
  @Path("paths/{path}/keys/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllVersionsForKey(
    @Context UriInfo uriInfo,
    @PathParam("path") String path,
    @PathParam("key") String key
  ) {
    Supplier<Response> supplier = () -> {
      List<SecretDto> secrets = this.secretRegistryPort.getSecrets(path, key)
        .stream()
        .map(SecretDto::toDto)
        .sorted(Comparator.comparing(SecretDto::getVersion))
        .collect(Collectors.toList());

      return Response.ok().entity(secrets).build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @GET
  @Path("paths/{path}/keys/{key}/versions")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listVersionsForKey(
    @Context UriInfo uriInfo,
    @PathParam("path") String path,
    @PathParam("key") String key
  ) {
    Supplier<Response> supplier = () -> {
      List<Integer> versions = this.secretRegistryPort.getVersions(path, key)
        .stream()
        .map(Version::getValue)
        .sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());

      return Response.ok().entity(versions).build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @GET
  @Path("paths/{path}/keys/{key}/versions/{version}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSecret(
    @Context UriInfo uriInfo,
    @PathParam("path") String path,
    @PathParam("key") String key,
    @PathParam("version") String version
  ) {
    Supplier<Response> supplier = () -> {
      SecretDto secret = this.secretRegistryPort.getSecret(path, key, version)
        .map(SecretDto::toDto)
        .orElseThrow(() -> new NotFoundException(String.format("Secret '%s/%s/%s' not found", path, key, version)));

      return Response.ok().entity(secret).build();
    };

    return handleExceptions(uriInfo, supplier);
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
    Supplier<Response> supplier = () -> {
      Secret createdSecret = this.secretRegistryPort.createSecret(path, key, createSecretDto.getSecret());

      URI resourceUri = uriInfo.getAbsolutePathBuilder()
        .path(String.valueOf(createdSecret.getVersion().getValue()))
        .build();

      return Response.created(resourceUri)
        .entity(SecretDto.toDto(createdSecret))
        .build();
    };

    return handleExceptions(uriInfo, supplier);
  }


  @DELETE
  @Path("paths/{path}/keys/{key}/versions/{version}")
  public Response deleteSpecificVersionForKey(
    @Context UriInfo uriInfo,
    @PathParam("path") String path,
    @PathParam("key") String key,
    @PathParam("version") String version
  ) {
    Supplier<Response> supplier = () -> {
      this.secretRegistryPort.deleteSecret(path, key, version);

      return Response.noContent().build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @DELETE
  @Path("paths/{path}/keys/{key}")
  public Response deleteAllVersionsForKey(
    @Context UriInfo uriInfo,
    @PathParam("path") String path,
    @PathParam("key") String key) {
    Supplier<Response> supplier = () -> {
      this.secretRegistryPort.deleteKey(path, key);

      return Response.noContent().build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  @DELETE
  @Path("paths/{path}")
  public Response deletePath(@Context UriInfo uriInfo, @PathParam("path") String path) {
    Supplier<Response> supplier = () -> {
      this.secretRegistryPort.deletePath(path);

      return Response.noContent().build();
    };

    return handleExceptions(uriInfo, supplier);
  }

  private static Response handleExceptions(UriInfo uriInfo, Supplier<Response> supplier) {
    try {
      return supplier.get();

    } catch (final Exception e) {
      return ApiExceptionHandler.toErrorResponse(uriInfo, e);
    }
  }
}
