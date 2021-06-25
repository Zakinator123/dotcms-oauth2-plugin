package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.exceptions.ConfigurationException;
import com.dotmarketing.util.Logger;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

@RequiredArgsConstructor
class OAuthProviderConnectionDetailsResolverFactory {

    private final ConfigurationPropertiesWrapper pluginProperties;

    OAuthProviderConnectionDetailsResolver getOAuthProviderConnectionDetailsResolver() {

        String oAuthProviderConnectionDetailsSource = pluginProperties.getRequiredProperty("OAUTH_PROVIDER_CONNECTION_DETAILS_SOURCE");
        String callbackPath = pluginProperties.getRequiredProperty("CALLBACK_PATH");

        switch (oAuthProviderConnectionDetailsSource) {
            case "AWS_PARAMETER_STORE":
                Logger.info(this, "Resolving OAuth Connection Details using AWS Parameter Store..");
                return new AwsParameterStoreOAuthProviderConnectionDetailsResolver(
                        getAwsParameterStoreClient(),
                        pluginProperties.getOptionalProperty("AWS_PARAMETER_STORE_ENVIRONMENT_PATH_PREFIX"),
                        pluginProperties.getRequiredProperty("OAUTH_PROVIDER_NAME"),
                        callbackPath);
            case "PROPERTIES_FILE":
                Logger.info(this, "Resolving OAuth Connection Details using properties file configuration..");
                return new LocalOAuthProviderConnectionDetailsResolver(pluginProperties, callbackPath);
            default:
                throw new ConfigurationException("Invalid Properties Source " + oAuthProviderConnectionDetailsSource);
        }
    }

    @NotNull
    private SsmClient getAwsParameterStoreClient() {
        Region awsRegion = Region.of(pluginProperties.getRequiredProperty("AWS_PARAMETER_STORE_REGION"));
        Logger.info(this, "Using the AWS Region '" + awsRegion.id() + "' to connect to AWS Parameter Store.");
        return SsmClient.builder().region((awsRegion)).build();
    }
}
