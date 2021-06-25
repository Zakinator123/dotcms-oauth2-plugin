package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.exceptions.ConfigurationException;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.provider.UserInfoClaimKeys;
import com.dotmarketing.util.Logger;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

@RequiredArgsConstructor
class AwsParameterStoreOAuthProviderConnectionDetailsResolver implements OAuthProviderConnectionDetailsResolver {

    private final SsmClient parameterStoreClient;
    private final String environment;
    private final String oAuthProviderName;
    private final String callbackPath;

    private String parameterStorePathPrefix;

    @Override
    public OAuthProviderConnectionDetails getOAuthProviderConnectionDetails() {

        buildAndSetParameterStorePathPrefix();

        // Required Connection Details
        final String clientId = getRequiredProperty("client-id");
        final String clientSecret = getRequiredProperty("client-secret") ;
        final String scopes = getRequiredProperty("scopes");
        final String providerBaseUrl = getRequiredProperty("provider-base-url");
        final String authorizationEndpoint = getRequiredProperty("authorization-endpoint-path");
        final String accessTokenEndpoint = getRequiredProperty("token-endpoint-path");
        final String userInfoEndpoint = getRequiredProperty("oidc-user-info-endpoint-path");

        OAuthProviderConnectionDetails build = OAuthProviderConnectionDetails.builder()
                .providerBaseUrl(providerBaseUrl)
                .accessTokenPath(accessTokenEndpoint)
                .authorizationPath(authorizationEndpoint)
                .userInfoPath(userInfoEndpoint)
                .callbackPath(callbackPath)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scopes(scopes)
                .userInfoClaimKeys(getUserInfoClaimKeys())
                .build();

        parameterStoreClient.close();

        return build;
    }


    private String getRequiredProperty(String key) {
        try {
            String parameter = getParameterStoreProperty(key);
            Logger.info(this, "Successfully fetched the AWS Parameter Store property at the following path: " + parameterStorePathPrefix + key);
            return parameter;
        } catch (AwsServiceException | SdkClientException e) {
            throw new ConfigurationException("The following required configuration could not be retrieved from AWS Parameter Store: " + key, e);
        }
    }

    private String getPropertyWithDefault(String key, String defaultPropertyValue) {
        try {
            String parameter = getParameterStoreProperty(key);
            Logger.info(this, "Successfully fetched the AWS Parameter Store property at the following path: " + parameterStorePathPrefix + key);
            return parameter;
        } catch (AwsServiceException | SdkClientException e) {
            Logger.info(this ,"Could not find the property '" + key + "' in AWS parameter store. Using the default value '" + defaultPropertyValue + "' instead.");
            return defaultPropertyValue;
        }
    }

    private String getParameterStoreProperty(String propertyName) throws AwsServiceException, SdkClientException {
        return parameterStoreClient.getParameter(GetParameterRequest.builder().name(parameterStorePathPrefix + propertyName).withDecryption(true).build()).parameter().value();
    }

    private void buildAndSetParameterStorePathPrefix() {
        parameterStorePathPrefix = String.format("%s/oauth/%s/", environment, oAuthProviderName);
    }

    private UserInfoClaimKeys getUserInfoClaimKeys() {
        // User Info Claim Keys - All of these default to the OpenID Connect Standard Claim Keys
        // See https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims for more information.
        final String groupsClaimKey = getPropertyWithDefault("groups-claim-key", "groups");
        final String emailClaimKey = getPropertyWithDefault("email-claim-key", "email");
        final String userIdClaimKey = getPropertyWithDefault("userid-claim-key", "sub");
        final String firstNameClaimKey = getPropertyWithDefault("first-name-claim-key", "given_name");
        final String lastNameClaimKey = getPropertyWithDefault("last-name-claim-key", "family_name");

        return UserInfoClaimKeys.builder()
                .email(emailClaimKey)
                .firstName(firstNameClaimKey)
                .lastName(lastNameClaimKey)
                .userId(userIdClaimKey)
                .groups(groupsClaimKey)
                .build();
    }
}
