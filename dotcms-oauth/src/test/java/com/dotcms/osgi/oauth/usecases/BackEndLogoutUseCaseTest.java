package com.dotcms.osgi.oauth.usecases;

import com.liferay.portal.util.WebKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scribe.model.OAuthConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BackEndLogoutUseCaseTest {

    private BackEndLogoutUseCase backEndLogoutUseCase;
    private HttpServletRequest mockRequest;
    private HttpSession mockSession;

    @BeforeEach
    public void beforeEach() {
        mockRequest = mock(HttpServletRequest.class);
        mockSession = mock(HttpSession.class);
        backEndLogoutUseCase = new BackEndLogoutUseCase();
    }

    @Test
    public void whenThereIsNoHttpSession_logoutUser_Returns_ALREADY_LOGGED_OUT() {
        when(mockRequest.getSession(false)).thenReturn(null);
        UseCaseResult useCaseResult = backEndLogoutUseCase.logoutUser(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.ALREADY_LOGGED_OUT);
    }

    @Test
    public void whenThereIsAnHttpSession_logoutUser_Returns_LOGOUT_SUCCESSFUL_And_RemovesAllSessionAttributes() {
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        UseCaseResult useCaseResult = backEndLogoutUseCase.logoutUser(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.BACK_END_LOGOUT_SUCCESSFUL);
        verify(mockSession).removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
        verify(mockSession).removeAttribute(OAuthConstants.ACCESS_TOKEN);
        verify(mockSession).removeAttribute(USER_EMAIL_SESSION_ATTRIBUTE);
        verify(mockSession).removeAttribute(USER_ID_SESSION_ATTRIBUTE);
        verify(mockSession).removeAttribute(WebKeys.USER_ID);
    }
}
