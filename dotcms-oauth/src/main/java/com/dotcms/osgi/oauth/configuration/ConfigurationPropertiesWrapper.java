package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.exceptions.ConfigurationException;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

@RequiredArgsConstructor
class ConfigurationPropertiesWrapper {
    private final Properties properties;

    String getRequiredProperty(String propertyKey) {
        String property = properties.getProperty(propertyKey);

        if (property == null) {
            throw new ConfigurationException("The required configuration property " + propertyKey + " could not be found.");
        }

        return property;
    }

    String getOptionalProperty(String propertyKey) {
        return properties.getProperty(propertyKey, "");
    }

    String getPropertyWithDefault(String propertyKey, String defaultProperty) {
        return properties.getProperty(propertyKey, defaultProperty);
    }
}
