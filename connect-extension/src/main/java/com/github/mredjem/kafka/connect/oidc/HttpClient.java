package com.github.mredjem.kafka.connect.oidc;

import com.github.mredjem.kafka.connect.AuthenticationCredentials;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpClient {

  private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();

  @Getter
  private final String baseUrl;
  private final String credentials;

  private HttpClient(String baseUrl, AuthenticationCredentials authenticationCredentials) {
    this.baseUrl = baseUrl.replaceAll("/$", "");
    this.credentials = authenticationCredentials.getAuthorization();
  }

  public static HttpClient create(String baseUrl, AuthenticationCredentials authenticationCredentials) {
    return new HttpClient(baseUrl, authenticationCredentials);
  }

  public <T> T doGET(String path, TypeToken<T> typeReference) {
    return this.doCall(path, HttpMethod.GET, MediaType.APPLICATION_JSON, "", typeReference);
  }

  public <T> T doPOST(String path, Map<String, String> parameters, TypeToken<T> typeReference) {
    String urlParameters = parameters.entrySet()
      .stream()
      .map(parameter -> parameter.getKey() + "=" + parameter.getValue())
      .collect(Collectors.joining("&"));

    return this.doCall(path, HttpMethod.POST, MediaType.APPLICATION_FORM_URLENCODED, urlParameters, typeReference);
  }

  private <T> T doCall(String path, String method, String contentType, String urlParameters, TypeToken<T> typeReference) {
    try {
      URL url = URI.create(this.baseUrl + path).toURL();

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(method);
      connection.setRequestProperty(HttpHeaders.AUTHORIZATION, this.credentials);
      connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, contentType);
      connection.setConnectTimeout(5_000);
      connection.setReadTimeout(5_000);
      connection.setDoOutput(true);

      if (urlParameters != null && !urlParameters.isEmpty()) {
        try (OutputStream outputStream = connection.getOutputStream()) {
          byte[] bodyBytes = urlParameters.getBytes(StandardCharsets.UTF_8);

          outputStream.write(bodyBytes);
          outputStream.flush();
        }
      }

      int responseCode = connection.getResponseCode();

      if (HttpURLConnection.HTTP_OK != responseCode) {
        throw new ClientErrorException(responseCode);
      }

      try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        StringBuilder response = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
          response.append(line);
        }

        return GSON.fromJson(response.toString(), typeReference.getType());
      }
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
