package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import com.dotcms.osgi.oauth.usecases.UseCaseResultEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OAuthInterceptorBaseClassTest {

    private OAuthInterceptorBaseClass oAuthInterceptorBaseClass;
    private HttpServletResponse mockResponse;
    private UseCaseResult mockUseCaseResult;

    @BeforeEach
    public void beforeEach() {
        oAuthInterceptorBaseClass = new OAuthInterceptorBaseClass();
        mockResponse = mock(HttpServletResponse.class);
        mockUseCaseResult = mock(UseCaseResult.class);
        when(mockUseCaseResult.getRedirectUri()).thenReturn("/");
    }

    @Test
    public void whenUseCaseResultRequiresNonEmptyRedirect_redirectOrContinueFilterChain_setsCorrectRedirectOnResponse() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED);
        when(mockUseCaseResult.getRedirectUri()).thenReturn("/test-redirect");
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/test-redirect");
    }

    @Test
    public void whenUseCaseResultHasEmptyRedirectString_redirectOrContinueFilterChain_SetsRedirectToRoot() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED);
        when(mockUseCaseResult.getRedirectUri()).thenReturn("");
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_SSO_AUTHENTICATION_REQUIRED_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_FRONT_END_LOGOUT_SUCCESSFUL_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.FRONT_END_LOGOUT_SUCCESSFUL);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_AUTHENTICATION_FAILURE_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.AUTHENTICATION_FAILURE);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_FRONT_END_AUTHENTICATION_SUCCESS_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.FRONT_END_AUTHENTICATION_SUCCESS);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_BACK_END_AUTHENTICATION_SUCCESS_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.BACK_END_AUTHENTICATION_SUCCESS);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
    }

    @Test
    public void whenUseCaseResultIs_AUTHENTICATION_FORBIDDEN_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.AUTHENTICATION_FORBIDDEN);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_ALREADY_LOGGED_INTO_FRONT_END_redirectOrContinueFilterChain_SkipsFilterChain_And_Redirects() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.ALREADY_LOGGED_INTO_FRONT_END);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.SKIP_NO_CHAIN);
        verify(mockResponse).sendRedirect("/");
    }

    @Test
    public void whenUseCaseResultIs_ALREADY_LOGGED_INTO_BACK_END_redirectOrContinueFilterChain_ContinuesFilterChain_And_DoesNotRedirect() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.ALREADY_LOGGED_INTO_BACK_END);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.NEXT);
        verifyZeroInteractions(mockResponse);
    }

    @Test
    public void whenUseCaseResultIs_NATIVE_AUTHENTICATION_REQUIRED_redirectOrContinueFilterChain_ContinuesFilterChain_And_DoesNotRedirect() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.NATIVE_AUTHENTICATION_REQUIRED);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.NEXT);
        verifyZeroInteractions(mockResponse);
    }

    @Test
    public void whenUseCaseResultIs_NO_AUTHENTICATION_REQUIRED_redirectOrContinueFilterChain_ContinuesFilterChain_And_DoesNotRedirect() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.NEXT);
        verifyZeroInteractions(mockResponse);
    }


    @Test
    public void whenUseCaseResultIs_LOGOUT_SUCCESSFUL_redirectOrContinueFilterChain_ContinuesFilterChain_And_DoesNotRedirect() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.BACK_END_LOGOUT_SUCCESSFUL);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.NEXT);
        verifyZeroInteractions(mockResponse);
    }

    @Test
    public void whenUseCaseResultIs_ALREADY_LOGGED_OUT_redirectOrContinueFilterChain_ContinuesFilterChain_And_DoesNotRedirect() throws IOException {
        when(mockUseCaseResult.getUseCaseResultEnum()).thenReturn(UseCaseResultEnum.ALREADY_LOGGED_OUT);
        Result result = oAuthInterceptorBaseClass.redirectOrContinueFilterChain(mockUseCaseResult, mockResponse);
        assertThat(result).isEqualTo(Result.NEXT);
        verifyZeroInteractions(mockResponse);
    }
}
