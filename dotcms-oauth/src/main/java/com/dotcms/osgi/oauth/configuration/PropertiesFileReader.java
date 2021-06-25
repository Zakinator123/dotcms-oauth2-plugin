package com.dotcms.osgi.oauth.configuration;

import com.dotmarketing.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class PropertiesFileReader {

    @NotNull Properties getPropertiesFromBundlePropertiesFile(String filePath) {

        Properties properties = new Properties();
        try {
            // Properties files that live within an OSGi bundle must be retrieved this way
            InputStream inputStream = PropertiesFileReader.class.getResourceAsStream(filePath);

            if (inputStream == null) {
                Logger.info(this.getClass(), "Configuration file " + filePath + " does not exist within the bundle.");
                return properties;
            }

            properties.load(inputStream);
        } catch (IOException e) {
            Logger.info(this.getClass(), "Configuration file " + filePath + " does not exist within the bundle.");
        }

        return properties;
    }

    Properties getPropertiesFromExternalPropertiesFile(String filePath) {

        Properties properties = new Properties();
        try {
            // For Properties files that live outside of the OSGi bundle
            properties.load(new FileInputStream(filePath));
        } catch (IOException e) {
            Logger.info(this.getClass(), "Configuration file " + filePath + " does not exist. Returning an empty properties set..");
        }

        return properties;
    }
}
