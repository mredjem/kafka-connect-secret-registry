package com.github.mredjem.kafka.connect.oidc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

public class HttpClient {

  private static final ObjectMapper OM = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private final String baseUrl;
  private final String credentials;

  private HttpClient(String baseUrl, String username, String password) {
    this.baseUrl = baseUrl.replaceAll("/$", "");
    this.credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
  }

  public static HttpClient create(String baseUrl, String username, String password) {
    return new HttpClient(baseUrl, username, password);
  }

  public <T> T doGET(String path, TypeReference<T> typeReference) {
    try {
      URL url = URI.create(String.format("%s/%s", this.baseUrl, path)).toURL();

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(HttpMethod.GET);
      connection.setRequestProperty(HttpHeaders.AUTHORIZATION, "Basic " + this.credentials);
      connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
      connection.setConnectTimeout(5_000);
      connection.setReadTimeout(5_000);
      connection.setDoOutput(true);

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

        return OM.readValue(response.toString(), typeReference);
      }
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
