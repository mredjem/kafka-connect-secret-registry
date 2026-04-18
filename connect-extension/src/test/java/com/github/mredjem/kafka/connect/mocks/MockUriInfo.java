package com.github.mredjem.kafka.connect.mocks;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class MockUriInfo implements UriInfo {

  private final String path;

  private MockUriInfo(String path) {
    this.path = path;
  }

  public static MockUriInfo of(String path) {
    return new MockUriInfo(path);
  }

  @Override
  public String getPath() {
    return this.path;
  }

  @Override
  public String getPath(boolean decode) {
    return this.path;
  }

  @Override
  public List<PathSegment> getPathSegments() {
    return Collections.emptyList();
  }

  @Override
  public List<PathSegment> getPathSegments(boolean decode) {
    return Collections.emptyList();
  }

  @Override
  public URI getRequestUri() {
    return URI.create(this.path);
  }

  @Override
  public UriBuilder getRequestUriBuilder() {
    return UriBuilder.fromUri(this.getRequestUri());
  }

  @Override
  public URI getAbsolutePath() {
    return URI.create("http://localhost:8080" + this.path);
  }

  @Override
  public UriBuilder getAbsolutePathBuilder() {
    return UriBuilder.fromUri(this.getAbsolutePath());
  }

  @Override
  public URI getBaseUri() {
    return URI.create("http://localhost:8080");
  }

  @Override
  public UriBuilder getBaseUriBuilder() {
    return UriBuilder.fromUri(this.getBaseUri());
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters() {
    return new MultivaluedHashMap<>();
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters(boolean decode) {
    return new MultivaluedHashMap<>();
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters() {
    return new MultivaluedHashMap<>();
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
    return new MultivaluedHashMap<>();
  }

  @Override
  public List<String> getMatchedURIs() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getMatchedURIs(boolean decode) {
    return Collections.emptyList();
  }

  @Override
  public List<Object> getMatchedResources() {
    return Collections.emptyList();
  }

  @Override
  public URI resolve(URI uri) {
    return null;
  }

  @Override
  public URI relativize(URI uri) {
    return null;
  }
}
