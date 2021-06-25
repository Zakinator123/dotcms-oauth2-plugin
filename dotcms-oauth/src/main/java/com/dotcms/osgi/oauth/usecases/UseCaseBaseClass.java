package com.dotcms.osgi.oauth.usecases;

import javax.servlet.http.HttpServletRequest;

public class UseCaseBaseClass {
    public String getCurrentHostNameForRedirectUri(final HttpServletRequest request) {

        // If a load balancer is in front of dotCMS, then the X-Forwarded-*
        // headers must be used to construct the redirectURI

        String hostName;
        String forwardedHost = request.getHeader("x-forwarded-host");
        if (forwardedHost == null) {
            hostName = request.getServerName();
        } else {
            hostName = forwardedHost;
        }

        String protocol;
        String forwardedProtocol = request.getHeader("x-forwarded-proto");
        if (forwardedProtocol == null) {
            protocol = request.getScheme();
        } else {
            protocol = forwardedProtocol;
        }

        return protocol + "://" + (
                (request.getServerPort() == 80 || request.getServerPort() == 443) ?
                        hostName
                        : hostName + ":" + request.getServerPort());
    }
}
