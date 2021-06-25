package com.dotcms.osgi.oauth.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scribe.model.OAuthConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class FrontEndLogoutUseCaseTest {

    private FrontEndLogoutUseCase frontEndLogoutUseCase;
    private HttpServletRequest mockRequest;
    private HttpSession mockSession;

    @BeforeEach
    public void beforeEach() {
        mockRequest = mock(HttpServletRequest.class);
        mockSession = mock(HttpSession.class);
        frontEndLogoutUseCase = new FrontEndLogoutUseCase();
    }

    @Test
    public void whenThereIsNoHttpSession_logoutUser_Returns_ALREADY_LOGGED_OUT() {
        when(mockRequest.getSession(false)).thenReturn(null);
        UseCaseResult useCaseResult = frontEndLogoutUseCase.logoutFrontEndUser(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.ALREADY_LOGGED_OUT);
    }

    @Test
    public void whenThereIsAnHttpSession_logoutUser_Returns_LOGOUT_SUCCESSFUL_withCorrectRedirectUri_And_RemovesAllSessionAttributes() {
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        UseCaseResult useCaseResult = frontEndLogoutUseCase.logoutFrontEndUser(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.FRONT_END_LOGOUT_SUCCESSFUL);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/logout-successful");
        verify(mockSession).removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        verify(mockSession).removeAttribute(OAuthConstants.ACCESS_TOKEN);
        verify(mockSession).removeAttribute(USER_EMAIL_SESSION_ATTRIBUTE);
        verify(mockSession).removeAttribute(USER_ID_SESSION_ATTRIBUTE);
        verify(mockSession).invalidate();
    }

    @Test
    public void whenThereIsAnHttpSession_logoutUser_Returns_LOGOUT_SUCCESSFUL_withCorrectCustomRedirectUri_And_RemovesAllSessionAttributes() {
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockRequest.getParameter(REFERRER)).thenReturn("/custom-redirect");
        UseCaseResult useCaseResult = frontEndLogoutUseCase.logoutFrontEndUser(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.FRONT_END_LOGOUT_SUCCESSFUL);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("/custom-redirect");
        verify(mockSession).removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        verify(mockSession).removeAttribute(OAuthConstants.ACCESS_TOKEN);
        verify(mockSession).removeAttribute(USER_EMAIL_SESSION_ATTRIBUTE);
        verify(mockSession).removeAttribute(USER_ID_SESSION_ATTRIBUTE);
        verify(mockSession).invalidate();
    }
}
