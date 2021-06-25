package com.dotcms.osgi.oauth.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;

import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
public abstract class OAuthProviderBaseClass implements OAuthProvider {

    private final OAuthProviderConnectionDetails connectionDetails;

    public OAuthProviderConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    public OAuthTokens extractOAuthTokens(String response) {
        Map<String, String> jsonResponse;
        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonResponse = mapper.readValue(response, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new OAuthException(
                    "Response body is incorrect. Can't extract a token from this: '"
                            + response
                            + "'", e);
        }

        String accessToken = jsonResponse.get("access_token");
        String idToken = jsonResponse.get("id_token");
        return new OAuthTokens(accessToken, idToken);
    }

    @Override
    public OAuthTokens getOAuthTokens(String authorizationCode, String callbackHost) {
        OAuthRequest request = this.buildAccessTokenRequest(new Verifier(authorizationCode), callbackHost);
        Response response = request.send();
        if (!response.isSuccessful()) {
            throw new OAuthException(
                    String.format("Unable to connect to end point [%s] [%s]",
                            this.getConnectionDetails().getAccessTokenEndpoint(),
                            response.getMessage()));
        }
        String responseBody = response.getBody();
        return this.extractOAuthTokens(responseBody);
    }

    @Override
    public UserInfo getOAuthUserInfo(OAuthTokens oAuthTokens) {

        String userInfoEndpoint = this.getConnectionDetails().getUserInfoEndpoint();

        final OAuthRequest userInfoRequest = this.buildUserInfoRequest(oAuthTokens, userInfoEndpoint);

        final Response userInfoResponse = userInfoRequest.send();

        if (!userInfoResponse.isSuccessful()) {
            throw new OAuthException(
                    String.format("Unable to connect to end point [%s] [%s]",
                            userInfoEndpoint,
                            userInfoResponse.getMessage()));
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, String> jsonResponse;
        try {
            jsonResponse = mapper.readValue(userInfoResponse.getBody(), new TypeReference<Map<String, String>>(){});
        } catch (JsonProcessingException e) {
            throw new OAuthException(
                    String.format("Unable to parse userinfo JSON response: [%s] [%s] [%s]",
                            e,
                            userInfoResponse.getMessage(),
                            userInfoResponse.getBody()));
        }

        UserInfoClaimKeys claimKeys = getConnectionDetails().getUserInfoClaimKeys();

        return UserInfo.builder()
                .email(jsonResponse.get(claimKeys.getEmail()))
                .firstName(jsonResponse.get(claimKeys.getFirstName()))
                .lastName(jsonResponse.get(claimKeys.getLastName()))
                .userGroups(Arrays.asList(jsonResponse.get(claimKeys.getGroups()).split(",")))
                .userId(jsonResponse.get(claimKeys.getUserId()))
                .build();
    }

    protected abstract OAuthRequest buildAccessTokenRequest(Verifier verifier, String callbackHostName);

    protected OAuthRequest buildUserInfoRequest(OAuthTokens oAuthTokens, String userInfoEndpoint) {
        final OAuthRequest userInfoRequest = new OAuthRequest(Verb.GET, userInfoEndpoint);
        userInfoRequest.addHeader("Authorization", "Bearer " + oAuthTokens.getAccessToken());
        return userInfoRequest;
    }
}
