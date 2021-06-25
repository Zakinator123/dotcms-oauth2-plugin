package com.dotcms.osgi.oauth;

import com.dotcms.osgi.oauth.configuration.PluginConfigurationProperties;
import com.dotcms.osgi.oauth.configuration.PluginConfigurator;
import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.interceptor.BackEndLoginRequiredInterceptor;
import com.dotcms.osgi.oauth.interceptor.BackEndLogoutInterceptor;
import com.dotcms.osgi.oauth.interceptor.OAuthCallbackInterceptor;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import lombok.Getter;
import okhttp3.mockwebserver.MockWebServer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public abstract class BaseIntegrationTest {

    private BackEndLoginRequiredInterceptor backEndLoginRequiredInterceptor;
    private OAuthCallbackInterceptor callbackInterceptor;
    private BackEndLogoutInterceptor backEndLogoutInterceptor;
    private PluginConfigurationProperties pluginConfiguration;

    static DotCmsFacade mockDotCmsFacade;
    static MockWebServer mockWebServer;

    static void setUpMockWebServer() {
        if (mockWebServer == null) {
            mockWebServer = new MockWebServer();
        }
    }

    void setUp(String defaultConfigurationFilePath, String externalConfigurationFileName, String oAuthProviderConfigurationPropertiesFileName) throws NoSuchFieldException, IllegalAccessException {
        mockDotCmsFacade = mock(DotCmsFacade.class);
        PluginConfigurator pluginConfigurator = new PluginConfigurator(
                mockDotCmsFacade,
                defaultConfigurationFilePath,
                externalConfigurationFileName,
                oAuthProviderConfigurationPropertiesFileName);

        pluginConfigurator.initializeObjectGraphDependencies();

        backEndLoginRequiredInterceptor = pluginConfigurator.createBackEndLoginRequiredInterceptor();
        callbackInterceptor = pluginConfigurator.createOAuthCallbackInterceptor();
        backEndLogoutInterceptor = pluginConfigurator.createBackEndLogoutInterceptor();

        pluginConfiguration = getPluginConfigurationProperties(pluginConfigurator);

        String mockOAuthProviderUrl = getMockOAuthProviderUrl(mockWebServer);
        setOAuthProviderHostToMockServer(mockOAuthProviderUrl);
    }

    private PluginConfigurationProperties getPluginConfigurationProperties(PluginConfigurator pluginConfigurator) throws NoSuchFieldException, IllegalAccessException {
        Field pluginConfigurationPropertiesField = pluginConfigurator.getClass().getDeclaredField("config");
        pluginConfigurationPropertiesField.setAccessible(true);
        return (PluginConfigurationProperties) pluginConfigurationPropertiesField.get(pluginConfigurator);
    }

    private String getMockOAuthProviderUrl(MockWebServer mockBackEnd) {
        return "http://localhost:" + mockBackEnd.getPort();
    }

    private void setOAuthProviderHostToMockServer(String mockOAuthProviderUrl) throws NoSuchFieldException, IllegalAccessException {
        Field oAuthProviderConnectionDetailsField = pluginConfiguration.getClass().getDeclaredField("oAuthProviderConnectionDetails");
        oAuthProviderConnectionDetailsField.setAccessible(true);
        OAuthProviderConnectionDetails connectionDetails = (OAuthProviderConnectionDetails) oAuthProviderConnectionDetailsField.get(pluginConfiguration);
        Field providerBaseUrlField = connectionDetails.getClass().getDeclaredField("providerBaseUrl");
        providerBaseUrlField.setAccessible(true);
        providerBaseUrlField.set(connectionDetails, mockOAuthProviderUrl);
    }

    void setUpMockRequestBaseUrlInformation(HttpServletRequest request) {
        when(request.getServerName()).thenReturn("dotcms-local.test");
        when(request.getScheme()).thenReturn("https");
        when(request.getServerPort()).thenReturn(443);
    }
}
