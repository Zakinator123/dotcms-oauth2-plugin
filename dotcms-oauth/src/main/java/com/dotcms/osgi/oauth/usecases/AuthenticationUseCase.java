package com.dotcms.osgi.oauth.usecases;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.mappers.GroupsToRolesMapper;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import com.dotcms.osgi.oauth.provider.OAuthTokens;
import com.dotcms.osgi.oauth.provider.UserInfo;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;

@RequiredArgsConstructor
public class AuthenticationUseCase extends UseCaseBaseClass {

    private final DotCmsFacade dotCmsFacade;
    private final OAuthProvider provider;
    private final GroupsToRolesMapper groupsToRolesMapper;
    private final List<String> defaultUserRoles;

    public UseCaseResult authenticateWithDotCms(HttpServletRequest request,
                                                HttpServletResponse response) {

        final String authorizationCode = request.getParameter(AUTHORIZATION_CODE_PARAMETER);

        if (authorizationCode == null || authorizationCode.isEmpty()) {
            return new UseCaseResult(UseCaseResultEnum.AUTHENTICATION_FAILURE, "/");
        }

        if (dotCmsFacade.dotCmsBackEndUserIsLoggedIn(request)) {
            return new UseCaseResult(UseCaseResultEnum.ALREADY_LOGGED_INTO_BACK_END);
        }

        String currentHostName = this.getCurrentHostNameForRedirectUri(request);
        final OAuthTokens oAuthTokens = provider.getOAuthTokens(authorizationCode, currentHostName);
        UserInfo userInfo = provider.getOAuthUserInfo(oAuthTokens);

        HttpSession httpSession = request.getSession(true);

        ////// Front End Login //////
        if (httpSession.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE) == FrontEndUserState.ATTEMPTING_LOGIN) {
            return logUserIntoFrontEnd(oAuthTokens, userInfo, httpSession);
        }

        ////// Back End Login //////
        return logUserIntoBackEnd(request, response, oAuthTokens, userInfo);
    }

    @NotNull
    private UseCaseResult logUserIntoBackEnd(HttpServletRequest request, HttpServletResponse response, OAuthTokens oAuthTokens, UserInfo userInfo) {
        List<String> rolesFromIdentityProvider = groupsToRolesMapper.mapUserGroupsToDotCmsRoles(userInfo.getUserGroups());
        if (defaultUserRoles.isEmpty() && rolesFromIdentityProvider.isEmpty()) {
            Logger.info(this, "The user has neither any roles from the SSO provider nor any default roles configured." +
                    " Authentication is forbidden.");
            return new UseCaseResult(UseCaseResultEnum.AUTHENTICATION_FORBIDDEN, "/");
        }

        User dotCmsBackEndUser = dotCmsFacade.getOrCreateBackEndUser(userInfo);
        if (!dotCmsBackEndUser.isActive()) {
            Logger.info(this, "The user is marked as inactive in the DotCMS user database. Authentication is forbidden.");
            return new UseCaseResult(UseCaseResultEnum.AUTHENTICATION_FORBIDDEN, "/");
        }

        addRolesToUser(dotCmsBackEndUser, rolesFromIdentityProvider);
        logUserIntoDotCmsBackEnd(request, response, dotCmsBackEndUser);

        // The session is retrieved again here since 'logUserIntoDotCmsBackEnd' results in the old session being invalidated
        // inside the dotCms facade call 'loginBackEndUser'
        HttpSession session = request.getSession(false);

        setSessionAttributes(oAuthTokens, userInfo, session);

        return new UseCaseResult(UseCaseResultEnum.BACK_END_AUTHENTICATION_SUCCESS, "/dotAdmin");
    }

    @NotNull
    private UseCaseResult logUserIntoFrontEnd(OAuthTokens oAuthTokens, UserInfo userInfo, HttpSession httpSession) {
        setSessionAttributes(oAuthTokens, userInfo, httpSession);
        String frontEndRedirectUrl = getFrontEndRedirectUrl(httpSession);
        Logger.info(this, "Front End Authentication successful.");
        return new UseCaseResult(UseCaseResultEnum.FRONT_END_AUTHENTICATION_SUCCESS, frontEndRedirectUrl);
    }

    private String getFrontEndRedirectUrl(HttpSession httpSession) {
        String frontEndRedirectUrl = (String) httpSession.getAttribute(OAUTH_REDIRECT_ATTRIBUTE);
        httpSession.removeAttribute(OAUTH_REDIRECT_ATTRIBUTE);
        return frontEndRedirectUrl;
    }

    private void logUserIntoDotCmsBackEnd(HttpServletRequest request, HttpServletResponse response, User dotCmsBackEndUser) {
        Logger.info(this, "Logging user into the DotCMS backend..");
        dotCmsFacade.loginBackEndUser(dotCmsBackEndUser, request, response);
        Logger.info(this, "Back End Authentication successful.");
    }

    private void setSessionAttributes(OAuthTokens oAuthTokens, UserInfo userInfo, HttpSession httpSession) {
        // Keep the token in session so that it can be revoked later in the logout interceptor or used by other plugins.
        httpSession.setAttribute(ACCESS_TOKEN_SESSION_ATTRIBUTE, oAuthTokens.getAccessToken());
        httpSession.setAttribute(USER_ID_SESSION_ATTRIBUTE, userInfo.getUserId());
        httpSession.setAttribute(USER_EMAIL_SESSION_ATTRIBUTE, userInfo.getEmail());
        httpSession.setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, FrontEndUserState.LOGGED_IN);
    }

    private void addRolesToUser(User user, List<String> rolesFromIdentityProvider) {
        Logger.info(this, "Removing all previous roles that existed on this user..");
        dotCmsFacade.removeAllUserRoles(user);
        dotCmsFacade.setUserRoles(user, defaultUserRoles);
        dotCmsFacade.setUserRoles(user, rolesFromIdentityProvider);
    }
}
