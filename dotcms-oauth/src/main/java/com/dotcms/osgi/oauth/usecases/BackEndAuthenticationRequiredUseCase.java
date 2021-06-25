package com.dotcms.osgi.oauth.usecases;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.List;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;

@RequiredArgsConstructor
public class BackEndAuthenticationRequiredUseCase extends UseCaseBaseClass {

    private final List<String> urlsThatDoNotRequireBackendAuthentication;
    private final DotCmsFacade dotCmsFacade;
    private final OAuthProvider provider;

    @NotNull
    public UseCaseResult determineIfAuthenticationIsRequired(HttpServletRequest request) {

        if (dotCmsFacade.dotCmsBackEndUserIsLoggedIn(request)) {
            return new UseCaseResult(UseCaseResultEnum.ALREADY_LOGGED_INTO_BACK_END);
        }

        final String requestedURI = request.getRequestURI().toLowerCase();
        if (requestedUrlDoesNotRequireAuthentication(requestedURI)) {
            return new UseCaseResult(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);
        }

        if (Boolean.parseBoolean(request.getParameter(NATIVE))) {
            return new UseCaseResult(UseCaseResultEnum.NATIVE_AUTHENTICATION_REQUIRED);
        }

        String redirectUrl = provider.getAuthorizationUrl(this.getCurrentHostNameForRedirectUri(request));

        removeFrontEndUserLoginAttemptFromSession(request);

        return new UseCaseResult(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED, redirectUrl);
    }

    private void removeFrontEndUserLoginAttemptFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object frontEndUserState = session.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        if (frontEndUserState != null && session.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE) == FrontEndUserState.ATTEMPTING_LOGIN) {
            session.removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        }
    }

    private boolean requestedUrlDoesNotRequireAuthentication(String requestedURI) {
        for (final String allowedSubPath : urlsThatDoNotRequireBackendAuthentication) {
            if (requestedURI.toLowerCase().matches(allowedSubPath.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
