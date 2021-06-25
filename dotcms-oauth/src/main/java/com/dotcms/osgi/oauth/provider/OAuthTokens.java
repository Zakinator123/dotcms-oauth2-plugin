package com.dotcms.osgi.oauth.provider;

import lombok.Data;

@Data
public class OAuthTokens {
    private final String accessToken;
    private final String idToken;
}
