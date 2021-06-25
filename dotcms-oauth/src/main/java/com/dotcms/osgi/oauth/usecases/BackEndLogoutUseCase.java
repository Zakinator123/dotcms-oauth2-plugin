package com.dotcms.osgi.oauth.usecases;

import com.dotmarketing.util.Logger;
import com.liferay.portal.util.WebKeys;
import org.scribe.model.OAuthConstants;

import javax.servlet.http.*;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;

public class BackEndLogoutUseCase {

    public UseCaseResult logoutUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return new UseCaseResult(UseCaseResultEnum.ALREADY_LOGGED_OUT);
        }

        Logger.info(this, "Logging out BackEnd user and removing all attributes from user session.");
        session.removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        session.removeAttribute(OAuthConstants.ACCESS_TOKEN);
        session.removeAttribute(USER_EMAIL_SESSION_ATTRIBUTE);
        session.removeAttribute(USER_ID_SESSION_ATTRIBUTE);
        session.removeAttribute(WebKeys.USER_ID);

        return new UseCaseResult(UseCaseResultEnum.BACK_END_LOGOUT_SUCCESSFUL);
    }
}
