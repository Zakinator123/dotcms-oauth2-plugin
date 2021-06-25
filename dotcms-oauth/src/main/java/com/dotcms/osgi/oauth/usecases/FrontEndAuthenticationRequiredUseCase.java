package com.dotcms.osgi.oauth.usecases;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.dotcms.osgi.oauth.usecases.FrontEndUserState.ATTEMPTING_LOGIN;
import static com.dotcms.osgi.oauth.usecases.FrontEndUserState.LOGGED_IN;
import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;

@RequiredArgsConstructor
public class FrontEndAuthenticationRequiredUseCase extends UseCaseBaseClass {

    private final DotCmsFacade dotCmsFacade;
    private final OAuthProvider provider;

    @NotNull
    public UseCaseResult determineIfAuthenticationIsRequired(HttpServletRequest request) {

        HttpSession session = request.getSession(true);

        if (dotCmsFacade.dotCmsBackEndUserIsLoggedIn(request)) {
            session.setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, LOGGED_IN);
            return new UseCaseResult(UseCaseResultEnum.ALREADY_LOGGED_INTO_FRONT_END, getPostAuthenticationRedirectUrl(request));
        }

        if (session.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE) == null ||
                session.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE).equals(ATTEMPTING_LOGIN)) {
            session.setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, ATTEMPTING_LOGIN);
        } else if (session.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE).equals(LOGGED_IN)){
            return new UseCaseResult(UseCaseResultEnum.ALREADY_LOGGED_INTO_FRONT_END, getPostAuthenticationRedirectUrl(request));
        } else {
            throw new IllegalArgumentException("The front end user state attribute "
                    + session.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE + " is not valid."));
        }

        session.setAttribute(OAUTH_REDIRECT_ATTRIBUTE, getPostAuthenticationRedirectUrl(request));

        String redirectUrlForAuthentication = provider.getAuthorizationUrl(this.getCurrentHostNameForRedirectUri(request));
        return new UseCaseResult(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED, redirectUrlForAuthentication);
    }

    private String getPostAuthenticationRedirectUrl(HttpServletRequest request) {
        String postAuthenticationRedirectUrl = request.getParameter(REFERRER);
        if (postAuthenticationRedirectUrl == null) {
            return "/";
        } else {
            return postAuthenticationRedirectUrl;
        }
    }
}
