package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.exceptions.ConfigurationException;
import com.dotmarketing.util.Logger;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

@RequiredArgsConstructor
class PluginConfigurationFileLoader {

    private final PropertiesFileReader propertiesFileReader;
    private final String defaultConfigurationFilePath;
    private final String externalConfigurationFileName;
    private final String oAuthProviderConfigurationFileName;

    @NotNull
    Properties getPluginConfigurationFileProperties() {
        Properties defaultPluginProperties = getDefaultConfigurationFileProperties();

        if (defaultPluginProperties.isEmpty()) {
            throw new ConfigurationException("Default plugin properties failed to load.");
        }

        Properties externalPluginProperties = getExternalConfigurationFileProperties(defaultPluginProperties);
        if (externalPluginProperties.isEmpty()) {
            Logger.info(this, "External configuration properties could not be found. Using default configuration..");
            return defaultPluginProperties;
        }

        Logger.info(this, "External configuration properties successfully found.");
        return externalPluginProperties;
    }

    @NotNull
    private Properties getDefaultConfigurationFileProperties() {
        Properties defaultPluginProperties = propertiesFileReader.getPropertiesFromBundlePropertiesFile(defaultConfigurationFilePath);
        Properties defaultOAuthProviderProperties = propertiesFileReader.getPropertiesFromBundlePropertiesFile("/" + oAuthProviderConfigurationFileName);
        defaultPluginProperties.putAll(defaultOAuthProviderProperties);
        return defaultPluginProperties;
    }

    @NotNull
    private Properties getExternalConfigurationFileProperties(Properties defaultPluginProperties) {
        String externalConfigurationDirectoryPath = defaultPluginProperties.getProperty("EXTERNAL_CONFIGURATION_DIRECTORY_PATH");
        Properties externalPluginProperties = propertiesFileReader.getPropertiesFromExternalPropertiesFile(
                externalConfigurationDirectoryPath + externalConfigurationFileName);

        if (externalPluginProperties.isEmpty()) {
            return externalPluginProperties;
        }

        Properties externalOAuthProviderProperties = propertiesFileReader.getPropertiesFromExternalPropertiesFile(
                externalConfigurationDirectoryPath + oAuthProviderConfigurationFileName);
        externalPluginProperties.putAll(externalOAuthProviderProperties);
        return externalPluginProperties;
    }
}
