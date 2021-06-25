package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.exceptions.ConfigurationException;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.provider.cognito.CognitoProvider;
import com.dotcms.osgi.oauth.provider.secureauth.SecureAuthProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class OAuthProviderFactory {

    private final String providerName;
    private final OAuthProviderConnectionDetails connectionDetails;

    OAuthProvider getOAuthProvider() {
        switch (providerName) {
            case "cognito":
                return new CognitoProvider(connectionDetails);
            case "secure-auth":
                return new SecureAuthProvider(connectionDetails);
            default:
                throw new ConfigurationException("No supporting OAuthProvider implementation exists for the provider " + providerName);
        }
    }
}
