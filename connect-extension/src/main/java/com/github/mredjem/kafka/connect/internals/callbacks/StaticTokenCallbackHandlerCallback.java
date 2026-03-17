package com.github.mredjem.kafka.connect.internals.callbacks;

import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.apache.kafka.common.security.oauthbearer.internals.secured.BasicOAuthBearerToken;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StaticTokenCallbackHandlerCallback implements AuthenticateCallbackHandler {

  public static final String SASL_OAUTHBEARER_ACCESS_TOKEN_CONFIG = "sasl.oauthbearer.access.token";

  private String token;

  @Override
  public void configure(Map<String, ?> configs, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
    this.token = (String) configs.get(SASL_OAUTHBEARER_ACCESS_TOKEN_CONFIG);

    if (this.token == null || this.token.isEmpty()) {
      throw new IllegalArgumentException("'" + SASL_OAUTHBEARER_ACCESS_TOKEN_CONFIG + "' is mandatory");
    }
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof OAuthBearerTokenCallback) {
        OAuthBearerToken parsedToken = new BasicOAuthBearerToken(this.token, new HashSet<>(), 0L, "", 0L);

        ((OAuthBearerTokenCallback) callback).token(parsedToken);
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }

  @Override
  public void close() {
    // noop
  }
}
