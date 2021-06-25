package com.dotcms.osgi.oauth.usecases;

import com.dotmarketing.util.Logger;
import org.scribe.model.OAuthConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;

public class FrontEndLogoutUseCase {

    public UseCaseResult logoutFrontEndUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return new UseCaseResult(UseCaseResultEnum.ALREADY_LOGGED_OUT);
        }

        Logger.info(this, "Logging out FrontEnd user and removing all attributes from user session.");
        session.removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        session.removeAttribute(OAuthConstants.ACCESS_TOKEN);
        session.removeAttribute(USER_EMAIL_SESSION_ATTRIBUTE);
        session.removeAttribute(USER_ID_SESSION_ATTRIBUTE);

        session.invalidate();

        return new UseCaseResult(UseCaseResultEnum.FRONT_END_LOGOUT_SUCCESSFUL, getPostAuthenticationRedirectUrl(request));
    }

    private String getPostAuthenticationRedirectUrl(HttpServletRequest request) {
        String postAuthenticationRedirectUrl = request.getParameter(REFERRER);
        if (postAuthenticationRedirectUrl == null) {
            return "/logout-successful";
        } else {
            return postAuthenticationRedirectUrl;
        }
    }
}
