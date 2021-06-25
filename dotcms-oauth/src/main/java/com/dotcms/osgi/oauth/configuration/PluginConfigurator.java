package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.interceptor.*;
import com.dotcms.osgi.oauth.mappers.GroupsToRolesMapper;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.usecases.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class PluginConfigurator {

    private final DotCmsFacade dotCmsFacade;
    private final String defaultConfigurationFileName;
    private final String externalConfigurationFileName;
    private final String oAuthProviderConfigurationFileName;

    private PluginConfigurationProperties config;

    private AuthenticationUseCase authenticationUseCase;
    private BackEndAuthenticationRequiredUseCase backEndAuthenticationRequiredUseCase;
    private FrontEndAuthenticationRequiredUseCase frontEndAuthenticationRequiredUseCase;
    private BackEndLogoutUseCase backendLogoutUseCase;
    private FrontEndLogoutUseCase frontEndLogoutUseCase;


    public void initializeObjectGraphDependencies() {

        buildAndSetPluginConfigurationProperties();

        OAuthProvider oAuthProvider = new OAuthProviderFactory(
                config.getOAuthProviderName(),
                config.getOAuthProviderConnectionDetails()).getOAuthProvider();

        GroupsToRolesMapper groupsToRolesMapper = new GroupsToRolesMapperFactory(
                config.getGroupsToRolesMappingStrategy(),
                config.getGroupsPrefixFilterList()).getGroupsToRolesMapper();

        authenticationUseCase = new AuthenticationUseCase(dotCmsFacade, oAuthProvider, groupsToRolesMapper, config.getDefaultDotCmsRoles());
        backEndAuthenticationRequiredUseCase = new BackEndAuthenticationRequiredUseCase(config.getUrlsThatDoNotRequireBackendAuthentication(), dotCmsFacade, oAuthProvider);
        frontEndAuthenticationRequiredUseCase = new FrontEndAuthenticationRequiredUseCase(dotCmsFacade, oAuthProvider);
        backendLogoutUseCase = new BackEndLogoutUseCase();
        frontEndLogoutUseCase = new FrontEndLogoutUseCase();
    }

    private void buildAndSetPluginConfigurationProperties() {

        ConfigurationPropertiesWrapper pluginProperties = getConfigurationProperties();

        String callbackPath = pluginProperties.getRequiredProperty("CALLBACK_PATH");
        boolean backendOAuthLoginEnabled = Boolean.parseBoolean(pluginProperties.getPropertyWithDefault("BACKEND_OAUTH_LOGIN_ENABLED", "true"));

        List<String> defaultDotCmsRoles = Arrays.asList(pluginProperties.getOptionalProperty("DEFAULT_DOTCMS_ROLES").split(","));
        List<String> groupsToRolesMappingPrefixFilterList = Arrays.asList(pluginProperties.getOptionalProperty("PREFIX_FILTER_LIST").split(","));
        List<String> urlsThatDoNotRequireBackendAuthentication = Arrays.asList(pluginProperties.getOptionalProperty("URLS_THAT_DO_NOT_REQUIRE_BACKEND_AUTHENTICATION").split(","));
        List<String> backEndLoginUrls = Arrays.asList(pluginProperties.getRequiredProperty("BACK_END_LOGIN_URLS").split(","));
        List<String> frontEndLoginUrls = Arrays.asList(pluginProperties.getRequiredProperty("FRONT_END_LOGIN_URLS").split(","));
        List<String> backEndLogoutUrls = Arrays.asList(pluginProperties.getRequiredProperty("BACK_END_LOGOUT_URLS").split(","));
        List<String> frontEndLogoutUrls = Arrays.asList(pluginProperties.getRequiredProperty("FRONT_END_LOGOUT_URLS").split(","));

        String oAuthProviderName = pluginProperties.getRequiredProperty("OAUTH_PROVIDER_NAME");
        String groupsToRolesMappingStrategy = pluginProperties.getPropertyWithDefault("GROUPS_TO_ROLES_MAPPING_STRATEGY", "SIMPLE");

        OAuthProviderConnectionDetailsResolver oAuthProviderConnectionDetailsResolver = new OAuthProviderConnectionDetailsResolverFactory(pluginProperties).getOAuthProviderConnectionDetailsResolver();
        OAuthProviderConnectionDetails oAuthProviderConnectionDetails = oAuthProviderConnectionDetailsResolver.getOAuthProviderConnectionDetails();

        this.config = PluginConfigurationProperties.builder()
                .callbackPath(callbackPath)
                .defaultDotCmsRoles(defaultDotCmsRoles)
                .groupsPrefixFilterList(groupsToRolesMappingPrefixFilterList)
                .groupsToRolesMappingStrategy(groupsToRolesMappingStrategy)
                .oAuthProviderConnectionDetails(oAuthProviderConnectionDetails)
                .oAuthProviderName(oAuthProviderName)
                .backEndLoginUrls(backEndLoginUrls)
                .frontEndLoginUrls(frontEndLoginUrls)
                .backEndLogoutUrls(backEndLogoutUrls)
                .frontEndLogoutUrls(frontEndLogoutUrls)
                .urlsThatDoNotRequireBackendAuthentication(urlsThatDoNotRequireBackendAuthentication)
                .backEndOAuthLoginEnabled(backendOAuthLoginEnabled)
                .build();
    }

    @NotNull
    private ConfigurationPropertiesWrapper getConfigurationProperties() {
        PluginConfigurationFileLoader pluginConfigurationFileLoader = new PluginConfigurationFileLoader(
                new PropertiesFileReader(),
                defaultConfigurationFileName,
                externalConfigurationFileName,
                oAuthProviderConfigurationFileName);

        return new ConfigurationPropertiesWrapper(
                pluginConfigurationFileLoader.getPluginConfigurationFileProperties());
    }

    public BackEndLoginRequiredInterceptor createBackEndLoginRequiredInterceptor() {
        return new BackEndLoginRequiredInterceptor(config.getBackEndLoginUrls(), backEndAuthenticationRequiredUseCase);
    }

    public FrontEndLoginRequiredInterceptor createFrontEndLoginRequiredInterceptor() {
        return new FrontEndLoginRequiredInterceptor(config.getFrontEndLoginUrls(), frontEndAuthenticationRequiredUseCase);
    }

    public OAuthCallbackInterceptor createOAuthCallbackInterceptor() {
        return new OAuthCallbackInterceptor(config.getCallbackPath(), authenticationUseCase);
    }

    public BackEndLogoutInterceptor createBackEndLogoutInterceptor() {
        return new BackEndLogoutInterceptor(backendLogoutUseCase, config.getBackEndLogoutUrls());
    }

    public FrontEndLogoutInterceptor createFrontEndLogoutInterceptor() {
        return new FrontEndLogoutInterceptor(frontEndLogoutUseCase, config.getFrontEndLogoutUrls());
    }

    public boolean getBackEndLoginEnabledFeatureFlag() {
        return config.isBackEndOAuthLoginEnabled();
    }
}
