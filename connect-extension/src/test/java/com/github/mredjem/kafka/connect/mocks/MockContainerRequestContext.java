package com.github.mredjem.kafka.connect.mocks;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

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
}
