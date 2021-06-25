package com.dotcms.osgi.oauth.provider.cognito;

import com.dotcms.osgi.oauth.provider.OAuthProviderBaseClass;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verifier;
import org.scribe.utils.OAuthEncoder;

import java.util.Base64;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.GRANT_TYPE;
import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.GRANT_TYPE_AUTHORIZATION_CODE;

public class CognitoProvider extends OAuthProviderBaseClass {

    public CognitoProvider(OAuthProviderConnectionDetails connectionDetails) {
        super(connectionDetails);
    }

    @Override
    public String getAuthorizationUrl(String callbackHostName) {
        String endpoint = this.getConnectionDetails().getAuthorizationEndpoint();
        String apiKeyQuery = String.format("client_id=%s&", this.getConnectionDetails().getClientId());
        String callbackQuery = String.format("redirect_uri=%s&", OAuthEncoder.encode(callbackHostName + this.getConnectionDetails().getCallbackPath()));
        String scopeQuery = String.format("scope=%s&", OAuthEncoder.encode(this.getConnectionDetails().getScopes()));

        return endpoint +
                "?" +
                apiKeyQuery +
                callbackQuery +
                scopeQuery +
                "response_type=code";
    }

    @Override
    protected OAuthRequest buildAccessTokenRequest(Verifier verifier, String callbackHostName) {
        OAuthRequest request = new OAuthRequest(
                this.getAccessTokenVerb(),
                this.getConnectionDetails().getAccessTokenEndpoint()
        );
        request.addHeader("Authorization", "Basic " + getCredentials());
        request.addBodyParameter(OAuthConstants.CLIENT_ID, this.getConnectionDetails().getClientId());
        request.addBodyParameter(OAuthConstants.REDIRECT_URI, callbackHostName + this.getConnectionDetails().getCallbackPath());
        request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
        request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
        return request;
    }

    private String getCredentials() {
        Base64.Encoder encoder = Base64.getEncoder();
        String credentials = this.getConnectionDetails().getClientId() + ":" + this.getConnectionDetails().getClientSecret();
        return encoder.encodeToString(credentials.getBytes());
    }
}
