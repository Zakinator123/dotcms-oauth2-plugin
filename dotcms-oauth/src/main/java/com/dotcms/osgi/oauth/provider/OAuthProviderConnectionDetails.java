package com.dotcms.osgi.oauth.provider;

import lombok.Builder;
import lombok.Getter;

@Builder
public class OAuthProviderConnectionDetails {
    private final String providerBaseUrl;
    private final String accessTokenPath;
    private final String authorizationPath;
    private final String userInfoPath;

    @Getter
    private final String callbackPath;

    @Getter
    private final String clientId;

    @Getter
    private final String clientSecret;

    @Getter
    private final String scopes;

    @Getter
    private final UserInfoClaimKeys userInfoClaimKeys;

    public String getAccessTokenEndpoint() {
        return providerBaseUrl + accessTokenPath;
    }

    public String getAuthorizationEndpoint() {
        return providerBaseUrl + authorizationPath;
    }

    public String getUserInfoEndpoint() {
        return providerBaseUrl + userInfoPath;
    }
}
