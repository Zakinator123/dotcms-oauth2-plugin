package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.provider.UserInfoClaimKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AwsParameterStoreOAuthProviderConnectionDetailsResolverTest {

    private static SsmClient mockParameterStoreClient;
    private static AwsParameterStoreOAuthProviderConnectionDetailsResolver awsParameterStoreOAuthProviderConnectionDetailsResolver;
    private static final String dotCmsEnvironment = "/dotcms-test";
    private static final String oAuthProviderName = "test-provider";
    private static final String callbackPath = "/callback";

    @BeforeEach
    public void beforeEach() {
        mockParameterStoreClient = mock(SsmClient.class);
        awsParameterStoreOAuthProviderConnectionDetailsResolver = new AwsParameterStoreOAuthProviderConnectionDetailsResolver(
                mockParameterStoreClient,
                dotCmsEnvironment,
                oAuthProviderName,
                callbackPath
        );
    }

    @Test
    public void getOAuthProviderConnectionDetails_withNoCustomUserInfoClaimKeysConfigured_returns_OAuthProviderConnectionDetails() {
        String parameterStorePathPrefix = String.format("%s/oauth/%s/", dotCmsEnvironment, oAuthProviderName);

        List<String> requiredConnectionDetailsKeys = Arrays.asList(
                "client-id",
                "client-secret",
                "scopes",
                "provider-base-url",
                "authorization-endpoint-path",
                "token-endpoint-path",
                "oidc-user-info-endpoint-path");

        List<String> optionalConnectionDetailsKeys = Arrays.asList(
                "groups-claim-key",
                "email-claim-key",
                "userid-claim-key",
                "first-name-claim-key",
                "last-name-claim-key");

        optionalConnectionDetailsKeys.forEach(key -> when(mockParameterStoreClient.getParameter(GetParameterRequest.builder().name(parameterStorePathPrefix + key).withDecryption(true).build())).thenThrow(ParameterNotFoundException.class));
        requiredConnectionDetailsKeys.forEach(key -> {
            when(mockParameterStoreClient.getParameter(GetParameterRequest.builder().name(parameterStorePathPrefix + key).withDecryption(true).build()))
                    .thenReturn(GetParameterResponse.builder().parameter(Parameter.builder().value(key + "-test-value").build()).build());
        });


        OAuthProviderConnectionDetails expectedOAuthProviderConnectionDetails = OAuthProviderConnectionDetails.builder()
                .providerBaseUrl("provider-base-url-test-value")
                .accessTokenPath("token-endpoint-path-test-value")
                .authorizationPath("authorization-endpoint-path-test-value")
                .callbackPath(callbackPath)
                .clientId("client-id-test-value")
                .clientSecret("client-secret-test-value")
                .scopes("scopes-test-value")
                .userInfoPath("oidc-user-info-endpoint-path-test-value")
                .userInfoClaimKeys(UserInfoClaimKeys.builder()
                        .email("email")
                        .firstName("given_name")
                        .groups("groups")
                        .lastName("family_name")
                        .userId("sub")
                        .build()).build();

        OAuthProviderConnectionDetails providerConnectionDetails = awsParameterStoreOAuthProviderConnectionDetailsResolver.getOAuthProviderConnectionDetails();
        assertThat(providerConnectionDetails).isEqualToComparingFieldByFieldRecursively(expectedOAuthProviderConnectionDetails);
        verify(mockParameterStoreClient).close();
    }
}
