package com.dotcms.osgi.oauth.usecases;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.mappers.GroupsToRolesMapper;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import com.dotcms.osgi.oauth.provider.OAuthTokens;
import com.dotcms.osgi.oauth.provider.UserInfo;
import com.liferay.portal.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AuthenticationUseCaseTest {

    private AuthenticationUseCase authenticationUseCase;
    private DotCmsFacade mockDotCmsFacade;
    private GroupsToRolesMapper mockGroupsToRolesMapper;
    private OAuthProvider mockOAuthProvider;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HttpSession mockSession;

    private List<String> defaultRoleList;

    @BeforeEach
    public void beforeEach() {
        mockDotCmsFacade = mock(DotCmsFacade.class);
        mockOAuthProvider = mock(OAuthProvider.class);
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockGroupsToRolesMapper = mock(GroupsToRolesMapper.class);

        mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(anyBoolean())).thenReturn(mockSession);

        defaultRoleList = Arrays.asList("default-role-1", "default-role-2");

        authenticationUseCase = new AuthenticationUseCase(mockDotCmsFacade, mockOAuthProvider, mockGroupsToRolesMapper, defaultRoleList);
    }

    @Test
    void whenAuthorizationCodeIsMissing_authenticateWithDotCms_Returns_AUTHENTICATION_FAILURE() {
        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.AUTHENTICATION_FAILURE);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/");
    }

    @Test
    void whenUserIsAlreadyLoggedIntoBackEnd_authenticateWithDotCms_Returns_ALREADY_LOGGED_INTO_BACK_END() {
        when(mockRequest.getParameter(AUTHORIZATION_CODE_PARAMETER)).thenReturn("1234");
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(true);

        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.ALREADY_LOGGED_INTO_BACK_END);
    }

    @Test
    void whenUserIsAttemptingFrontEndLogin_authenticateWithDotCms_Returns_AUTHENTICATION_SUCCESS_AndSetsCorrectSessionAttributes() {
        when(mockRequest.getParameter(AUTHORIZATION_CODE_PARAMETER)).thenReturn("1234");
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockSession.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE)).thenReturn(FrontEndUserState.ATTEMPTING_LOGIN);
        when(mockSession.getAttribute(OAUTH_REDIRECT_ATTRIBUTE)).thenReturn("/test-front-end-redirect");

        when(mockRequest.getServerName()).thenReturn("dotcms-local.test");
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerPort()).thenReturn(443);

        OAuthTokens mockOAuthTokens = mock(OAuthTokens.class);

        when(mockOAuthProvider.getOAuthTokens("1234", "https://dotcms-local.test")).thenReturn(mockOAuthTokens);
        when(mockOAuthProvider.getOAuthUserInfo(mockOAuthTokens)).thenReturn(mock(UserInfo.class));

        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);

        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.FRONT_END_AUTHENTICATION_SUCCESS);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/test-front-end-redirect");
        verify(mockSession).setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, FrontEndUserState.LOGGED_IN);
        verifyZeroInteractions(mockGroupsToRolesMapper);
    }


    @Test
    void whenUserIsAttemptingBackEndLogin_authenticateWithDotCms_Returns_AUTHENTICATION_SUCCESS_AndLogsUserIntoDotCms() {
        when(mockRequest.getParameter(AUTHORIZATION_CODE_PARAMETER)).thenReturn("1234");
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockRequest.getServerName()).thenReturn("dotcms-local.test");
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerPort()).thenReturn(443);

        OAuthTokens mockOAuthTokens = mock(OAuthTokens.class);

        when(mockOAuthProvider.getOAuthTokens("1234", "https://dotcms-local.test")).thenReturn(mockOAuthTokens);
        when(mockOAuthProvider.getOAuthUserInfo(mockOAuthTokens)).thenReturn(mock(UserInfo.class));

        User mockDotCmsUser = mock(User.class);
        when(mockDotCmsFacade.getOrCreateBackEndUser(any())).thenReturn(mockDotCmsUser);
        when(mockDotCmsUser.isActive()).thenReturn(true);

        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);

        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.BACK_END_AUTHENTICATION_SUCCESS);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/dotAdmin");
        verify(mockGroupsToRolesMapper).mapUserGroupsToDotCmsRoles(any());
        verify(mockDotCmsFacade).loginBackEndUser(mockDotCmsUser, mockRequest, mockResponse);
        verify(mockDotCmsFacade).setUserRoles(mockDotCmsUser, defaultRoleList);
    }

    @Test
    void whenUserIsAttemptingBackEndLogin_authenticateWithDotCms_Returns_AUTHENTICATION_FORBIDDEN_whenDotCmsUserIsInactive() {
        when(mockRequest.getParameter(AUTHORIZATION_CODE_PARAMETER)).thenReturn("1234");
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockRequest.getServerName()).thenReturn("dotcms-local.test");
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerPort()).thenReturn(443);

        OAuthTokens mockOAuthTokens = mock(OAuthTokens.class);

        when(mockOAuthProvider.getOAuthTokens("1234", "https://dotcms-local.test")).thenReturn(mockOAuthTokens);
        when(mockOAuthProvider.getOAuthUserInfo(mockOAuthTokens)).thenReturn(mock(UserInfo.class));

        User mockDotCmsUser = mock(User.class);
        when(mockDotCmsFacade.getOrCreateBackEndUser(any())).thenReturn(mockDotCmsUser);
        when(mockDotCmsUser.isActive()).thenReturn(false);

        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);

        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.AUTHENTICATION_FORBIDDEN);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/");
    }

    @Test
    void whenUserIsAttemptingBackEndLogin_authenticateWithDotCms_Returns_AUTHENTICATION_FORBIDDEN_whenUserHasNoRoles() {
        authenticationUseCase = new AuthenticationUseCase(mockDotCmsFacade, mockOAuthProvider, mockGroupsToRolesMapper, Collections.emptyList());

        when(mockRequest.getParameter(AUTHORIZATION_CODE_PARAMETER)).thenReturn("1234");
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockRequest.getServerName()).thenReturn("dotcms-local.test");
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerPort()).thenReturn(443);

        OAuthTokens mockOAuthTokens = mock(OAuthTokens.class);

        when(mockOAuthProvider.getOAuthTokens("1234", "https://dotcms-local.test")).thenReturn(mockOAuthTokens);
        when(mockOAuthProvider.getOAuthUserInfo(mockOAuthTokens)).thenReturn(mock(UserInfo.class));

        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);

        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.AUTHENTICATION_FORBIDDEN);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/");
    }

    @Test
    void whenUserIsAttemptingBackEndLogin_authenticateWithDotCms_Returns_BACK_END_AUTHENTICATION_SUCCESS_whenUserHasRolesFromSSOProvider() {
        authenticationUseCase = new AuthenticationUseCase(mockDotCmsFacade, mockOAuthProvider, mockGroupsToRolesMapper, Collections.emptyList());

        when(mockRequest.getParameter(AUTHORIZATION_CODE_PARAMETER)).thenReturn("1234");
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockRequest.getServerName()).thenReturn("dotcms-local.test");
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerPort()).thenReturn(443);

        OAuthTokens mockOAuthTokens = mock(OAuthTokens.class);

        when(mockOAuthProvider.getOAuthTokens("1234", "https://dotcms-local.test")).thenReturn(mockOAuthTokens);
        when(mockOAuthProvider.getOAuthUserInfo(mockOAuthTokens)).thenReturn(mock(UserInfo.class));

        when(mockGroupsToRolesMapper.mapUserGroupsToDotCmsRoles(any())).thenReturn(Arrays.asList("test-role1", "test-role2"));

        User mockDotCmsUser = mock(User.class);
        when(mockDotCmsFacade.getOrCreateBackEndUser(any())).thenReturn(mockDotCmsUser);
        when(mockDotCmsUser.isActive()).thenReturn(true);

        UseCaseResult useCaseResult = authenticationUseCase.authenticateWithDotCms(mockRequest, mockResponse);

        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.BACK_END_AUTHENTICATION_SUCCESS);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/dotAdmin");
    }
}
