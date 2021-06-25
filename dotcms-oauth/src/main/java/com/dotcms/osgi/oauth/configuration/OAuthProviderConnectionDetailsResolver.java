package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;

interface OAuthProviderConnectionDetailsResolver {
    OAuthProviderConnectionDetails getOAuthProviderConnectionDetails();
}
