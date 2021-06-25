package com.dotcms.osgi.oauth.provider.secureauth;

import com.dotcms.osgi.oauth.provider.OAuthProviderBaseClass;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.provider.OAuthTokens;
import com.dotcms.osgi.oauth.util.OAuthPluginConstants;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.utils.OAuthEncoder;

import static org.scribe.model.OAuthConstants.REDIRECT_URI;

public class SecureAuthProvider extends OAuthProviderBaseClass {

    public SecureAuthProvider(OAuthProviderConnectionDetails connectionDetails) {
        super(connectionDetails);
    }

    @Override
    protected OAuthRequest buildUserInfoRequest(OAuthTokens oAuthTokens, String userInfoEndpoint) {
        final OAuthRequest userInfoRequest = new OAuthRequest(Verb.POST, userInfoEndpoint);
        userInfoRequest.addBodyParameter("access_token", oAuthTokens.getAccessToken());
        return userInfoRequest;
    }

    @Override
    public String getAuthorizationUrl(String callbackHostName) {

        OAuthProviderConnectionDetails connectionDetails = this.getConnectionDetails();

        String endpoint = connectionDetails.getAuthorizationEndpoint();
        String clientIdParameter = String.format("client_id=%s&", connectionDetails.getClientId());
        String clientSecretParameter = String.format("client_secret=%s&", connectionDetails.getClientSecret());
        String redirectUriParameter = String.format("redirect_uri=%s&", OAuthEncoder.encode(callbackHostName + connectionDetails.getCallbackPath()));
        String scopesParameter = String.format("scope=%s&", OAuthEncoder.encode(connectionDetails.getScopes()));

        return endpoint +
                "?" +
                clientIdParameter +
                clientSecretParameter +
                redirectUriParameter +
                scopesParameter +
                "response_type=code";
    }

    @Override
    protected OAuthRequest buildAccessTokenRequest(Verifier verifier, String callbackHost) {

        OAuthProviderConnectionDetails properties = this.getConnectionDetails();

        OAuthRequest request = new OAuthRequest(
                this.getAccessTokenVerb(),
                properties.getAccessTokenEndpoint()
        );
        request.addBodyParameter(OAuthConstants.CLIENT_ID, properties.getClientId());
        request.addBodyParameter(OAuthConstants.CLIENT_SECRET, properties.getClientSecret());

        request.addBodyParameter(REDIRECT_URI, callbackHost + properties.getCallbackPath());

        request.addBodyParameter(OAuthPluginConstants.GRANT_TYPE, OAuthPluginConstants.GRANT_TYPE_AUTHORIZATION_CODE);
        request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
        request.addBodyParameter(OAuthConstants.SCOPE, properties.getScopes());
        return request;
    }
}
