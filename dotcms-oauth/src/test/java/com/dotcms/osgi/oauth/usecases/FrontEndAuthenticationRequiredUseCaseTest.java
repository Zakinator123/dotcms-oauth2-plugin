package com.dotcms.osgi.oauth.usecases;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.dotcms.osgi.oauth.usecases.FrontEndUserState.ATTEMPTING_LOGIN;
import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FrontEndAuthenticationRequiredUseCaseTest {
    private FrontEndAuthenticationRequiredUseCase frontEndAuthenticationRequiredUseCase;
    private DotCmsFacade mockDotCmsFacade;
    private OAuthProvider mockOAuthProvider;
    private HttpServletRequest mockRequest;

    @BeforeEach
    public void beforeEach() {
        mockDotCmsFacade = mock(DotCmsFacade.class);
        mockOAuthProvider = mock(OAuthProvider.class);
        mockRequest = mock(HttpServletRequest.class);

        frontEndAuthenticationRequiredUseCase = new FrontEndAuthenticationRequiredUseCase(mockDotCmsFacade, mockOAuthProvider);
    }

    @Test
    public void whenUserIsLoggedIntoDotCmsBackend_determineIfAuthenticationIsRequired_LogsUserIntoFrontEnd_And_Returns_ALREADY_LOGGED_INTO_FRONT_END() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(true);
        UseCaseResult useCaseResult = frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.ALREADY_LOGGED_INTO_FRONT_END);
        verify(mockSession).setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, FrontEndUserState.LOGGED_IN);
    }

    @Test
    public void whenUserIsLoggedIntoFrontEnd_determineIfAuthenticationIsRequired_Returns_ALREADY_LOGGED_INTO_FRONT_END() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockSession.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE)).thenReturn(FrontEndUserState.LOGGED_IN);
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        UseCaseResult useCaseResult = frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.ALREADY_LOGGED_INTO_FRONT_END);
    }

    @Test
    public void whenUserIsNotLoggedIntoFrontEnd_determineIfAuthenticationIsRequired_Returns_SSO_AUTHENTICATION_REQUIRED_withRedirectUrl() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        when(mockOAuthProvider.getAuthorizationUrl(any())).thenReturn("https://test/authorize/?test1=1");
        UseCaseResult useCaseResult = frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("https://test/authorize/?test1=1");
    }

    @Test
    public void whenUserIsNotLoggedIntoFrontEnd_determineIfAuthenticationIsRequired_SetsCorrectSessionAttributes_IfNoPostAuthenticationRedirectIsSpecified() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);

        verify(mockSession).setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, ATTEMPTING_LOGIN);
        verify(mockSession).setAttribute(OAUTH_REDIRECT_ATTRIBUTE, "/");
    }

    @Test
    public void whenUserIsNotLoggedIntoFrontEnd_determineIfAuthenticationIsRequired_SetsCorrectSessionAttributes_IfStatusIs_ATTEMPTING_LOGIN() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockSession.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE)).thenReturn(ATTEMPTING_LOGIN);
        frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);

        verify(mockSession).setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, ATTEMPTING_LOGIN);
        verify(mockSession).setAttribute(OAUTH_REDIRECT_ATTRIBUTE, "/");
    }

    @Test
    public void whenUserIsNotLoggedIntoFrontEnd_determineIfAuthenticationIsRequired_SetsCorrectSessionAttributes_IfPostAuthenticationRedirectIsSpecified() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        when(mockRequest.getParameter(REFERRER)).thenReturn("/test-reports/url");
        frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);

        verify(mockSession).setAttribute(FRONT_END_USER_STATE_ATTRIBUTE, ATTEMPTING_LOGIN);
        verify(mockSession).setAttribute(OAUTH_REDIRECT_ATTRIBUTE, "/test-reports/url");
    }

    @Test
    public void whenUserHasUnrecognizedFrontEndUserState_determineIfAuthenticationIsRequired_Throws_IllegalArgumentException() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);

        when(mockSession.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE)).thenReturn("this should be invalid");

        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest));
    }
}
