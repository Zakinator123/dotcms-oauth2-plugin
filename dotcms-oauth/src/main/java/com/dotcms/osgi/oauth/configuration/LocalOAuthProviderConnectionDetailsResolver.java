package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.provider.UserInfoClaimKeys;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class LocalOAuthProviderConnectionDetailsResolver implements OAuthProviderConnectionDetailsResolver {

    private final ConfigurationPropertiesWrapper pluginProperties;
    private final String callbackPath;

    @Override
    public OAuthProviderConnectionDetails getOAuthProviderConnectionDetails() {

        // Required Connection Details
        final String clientId = pluginProperties.getRequiredProperty("CLIENT_ID");
        final String clientSecret = pluginProperties.getRequiredProperty("CLIENT_SECRET") ;
        final String scopes = pluginProperties.getRequiredProperty("SCOPES");
        final String providerBaseUrl = pluginProperties.getRequiredProperty("OAUTH_PROVIDER_BASE_URL");
        final String authorizationEndpoint = pluginProperties.getRequiredProperty("AUTHORIZATION_ENDPOINT_PATH");
        final String accessTokenEndpoint = pluginProperties.getRequiredProperty("ACCESS_TOKEN_ENDPOINT_PATH");
        final String userInfoEndpoint = pluginProperties.getRequiredProperty("USER_INFO_ENDPOINT_PATH");

        UserInfoClaimKeys claimKeys = getUserInfoClaimKeys();

        return OAuthProviderConnectionDetails.builder()
                .providerBaseUrl(providerBaseUrl)
                .accessTokenPath(accessTokenEndpoint)
                .authorizationPath(authorizationEndpoint)
                .userInfoPath(userInfoEndpoint)
                .callbackPath(callbackPath)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scopes(scopes)
                .userInfoClaimKeys(claimKeys)
                .build();
    }

    private UserInfoClaimKeys getUserInfoClaimKeys() {
        // User Info Claim Keys - All of these default to the OpenID Connect Standard Claim Keys
        // See https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims for more information.
        final String groupsClaimKey = pluginProperties.getPropertyWithDefault("GROUPS_CLAIM_KEY", "groups");
        final String emailClaimKey = pluginProperties.getPropertyWithDefault("EMAIL_CLAIM_KEY", "email");
        final String userIdClaimKey = pluginProperties.getPropertyWithDefault("USERID_CLAIM_KEY", "sub");
        final String firstNameClaimKey = pluginProperties.getPropertyWithDefault("FIRST_NAME_CLAIM_KEY", "given_name");
        final String lastNameClaimKey = pluginProperties.getPropertyWithDefault("LAST_NAME_CLAIM_KEY", "family_name");

        return UserInfoClaimKeys.builder()
                .email(emailClaimKey)
                .firstName(firstNameClaimKey)
                .lastName(lastNameClaimKey)
                .userId(userIdClaimKey)
                .groups(groupsClaimKey)
                .build();
    }
}
