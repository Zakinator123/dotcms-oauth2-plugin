package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PluginConfigurationProperties {
    private final String oAuthProviderName;
    private final List<String> defaultDotCmsRoles;
    private final String callbackPath;
    private final String groupsToRolesMappingStrategy;
    private final List<String> groupsPrefixFilterList;
    private final OAuthProviderConnectionDetails oAuthProviderConnectionDetails;
    private final List<String> backEndLoginUrls;
    private final List<String> frontEndLoginUrls;
    private final List<String> backEndLogoutUrls;
    private final List<String> frontEndLogoutUrls;
    private final List<String> urlsThatDoNotRequireBackendAuthentication;

    private final boolean backEndOAuthLoginEnabled;
}
