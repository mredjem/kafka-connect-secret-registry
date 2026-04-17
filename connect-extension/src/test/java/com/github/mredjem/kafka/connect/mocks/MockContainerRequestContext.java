package com.github.mredjem.kafka.connect.mocks;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class MockContainerRequestContext implements ContainerRequestContext {

  private final String method;

  private final MockUriInfo uriInfo;

  private final Map<String, String> headers;

  private final String requestBody;

  private final List<Consumer<Response>> assertions = new ArrayList<>();

  private MockContainerRequestContext(String method, String path, Map<String, String> headers, String requestBody) {
    this.method = method;
    this.uriInfo = MockUriInfo.of(path);
    this.headers = Collections.unmodifiableMap(headers);
    this.requestBody = requestBody;
  }

  public static MockContainerRequestContext of(String method, String path, Map<String, String> headers) {
    return of(method, path, headers, null);
  }

  public static MockContainerRequestContext of(String method, String path, Map<String, String> headers, String requestBody) {
    return new MockContainerRequestContext(method, path, headers, requestBody);
  }

  public void addAssertion(Consumer<Response> assertion) {
    this.assertions.add(assertion);
  }

  @Override
  public Object getProperty(String name) {
    return null;
  }

  @Override
  public Collection<String> getPropertyNames() {
    return Collections.emptyList();
  }

  @Override
  public void setProperty(String name, Object object) {
    // noop
  }

  @Override
  public void removeProperty(String name) {
    // noop
  }

  @Override
  public UriInfo getUriInfo() {
    return this.uriInfo;
  }

  @Override
  public void setRequestUri(URI requestUri) {
    // noop
  }

  @Override
  public void setRequestUri(URI baseUri, URI requestUri) {
    // noop
  }

  @Override
  public Request getRequest() {
    return null;
  }

  @Override
  public String getMethod() {
    return this.method;
  }

  @Override
  public void setMethod(String method) {
    // noop
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return new MultivaluedHashMap<>(this.headers);
  }

  @Override
  public String getHeaderString(String name) {
    return this.headers.get(name);
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public Locale getLanguage() {
    return Locale.ENGLISH;
  }

  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public MediaType getMediaType() {
    return MediaType.APPLICATION_JSON_TYPE;
  }

  @Override
  public List<MediaType> getAcceptableMediaTypes() {
    return Collections.emptyList();
  }

  @Override
  public List<Locale> getAcceptableLanguages() {
    return Collections.emptyList();
  }

  @Override
  public Map<String, Cookie> getCookies() {
    return Collections.emptyMap();
  }

  @Override
  public boolean hasEntity() {
    return this.requestBody != null && !this.requestBody.isEmpty();
  }

  @Override
  public InputStream getEntityStream() {
    if (this.hasEntity()) {
      return new ByteArrayInputStream(this.requestBody.getBytes());
    }

    return null;
  }

  @Override
  public void setEntityStream(InputStream input) {
    // noop
  }

  @Override
  public SecurityContext getSecurityContext() {
    return null;
  }

  @Override
  public void setSecurityContext(SecurityContext context) {
    // noop
  }

  @Override
  public void abortWith(Response response) {
    this.assertions.forEach(assertion -> assertion.accept(response));
  }

  public static class MockUriInfo implements UriInfo {

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
}
