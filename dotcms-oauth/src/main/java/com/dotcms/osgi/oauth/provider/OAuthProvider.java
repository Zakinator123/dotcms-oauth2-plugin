package com.dotcms.osgi.oauth.provider;

public interface OAuthProvider {
    String getAuthorizationUrl(String callbackHostName);
    OAuthTokens getOAuthTokens(String authorizationCode, String callbackHost);
    UserInfo getOAuthUserInfo(OAuthTokens oAuthTokens);
    default void revokeToken(final String token) {}
}
