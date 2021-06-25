package com.dotcms.osgi.oauth.usecases;

import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.provider.OAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.FRONT_END_USER_STATE_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BackEndAuthenticationRequiredUseCaseTest {

    private static final String[] URLS_THAT_DO_NOT_REQUIRE_BACKEND_AUTHENTICATION =
            new String[]{
                    ".*\\.chunk\\..*",
                    "\\/loginform",
                    ".*\\.woff",
                    "\\/dotadmin\\/assets\\/icon",
                    "\\/dotAdmin\\/styles.*.css",
            };

    private BackEndAuthenticationRequiredUseCase backEndAuthenticationRequiredUseCase;
    private DotCmsFacade mockDotCmsFacade;
    private OAuthProvider mockOAuthProvider;
    private HttpServletRequest mockRequest;
    private HttpSession mockSession;

    @BeforeEach
    public void beforeEach() {
        mockDotCmsFacade = mock(DotCmsFacade.class);
        mockOAuthProvider = mock(OAuthProvider.class);
        mockRequest = mock(HttpServletRequest.class);
        mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        backEndAuthenticationRequiredUseCase = new BackEndAuthenticationRequiredUseCase(Arrays.asList(URLS_THAT_DO_NOT_REQUIRE_BACKEND_AUTHENTICATION), mockDotCmsFacade, mockOAuthProvider);
    }

    @Test
    public void whenUrlDoesNotRequireAuthentication_determineIfAuthenticationIsRequired_Returns_NO_AUTHENTICATION_REQUIRED() {
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin/assets/icon");
        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);

        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin/assets/icon");
        useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);

        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin/styles.234lkj23asdfzsd.css");
        useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);

        when(mockRequest.getRequestURI()).thenReturn("12341234.chunk.12341234");
        useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);

        when(mockRequest.getRequestURI()).thenReturn("test-font.woff");
        useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.NO_AUTHENTICATION_REQUIRED);
    }


    @Test
    public void whenUserIsLoggedIntoDotCms_determineIfAuthenticationIsRequired_Returns_ALREADY_LOGGED_IN() {
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(true);
        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.ALREADY_LOGGED_INTO_BACK_END);

    }

    @Test
    public void whenUrlContainsNativeLoginParameter_determineIfAuthenticationIsRequired_Returns_NATIVE_LOGIN_REQUIRED() {
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);

        when(mockRequest.getParameter("native")).thenReturn("true");
        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin");

        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.NATIVE_AUTHENTICATION_REQUIRED);
    }

    @Test
    public void whenAuthenticationIsRequired_backEndAuthenticationRequiredUseCase_Returns_SSO_AUTHENTICATION_REQUIRED() {
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin");
        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED);
    }

    @Test
    public void whenAuthenticationIsRequired_And_FrontEndUserState_Is_ATTEMPTING_LOGIN_backEndAuthenticationRequiredUseCase_Removes_FrontEndUserState_Attribute() {
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin");
        when(mockSession.getAttribute(FRONT_END_USER_STATE_ATTRIBUTE)).thenReturn(FrontEndUserState.ATTEMPTING_LOGIN);
        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getUseCaseResultEnum()).isEqualTo(UseCaseResultEnum.SSO_AUTHENTICATION_REQUIRED);
        verify(mockSession).removeAttribute(FRONT_END_USER_STATE_ATTRIBUTE);
    }

    @Test
    public void whenAuthenticationIsRequired_determineIfAuthenticationIsRequired_Returns_CorrectRedirectUrl() {
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(mockRequest)).thenReturn(false);
        when(mockRequest.getRequestURI()).thenReturn("/dotAdmin");

        when(mockRequest.getServerName()).thenReturn("dotcms-local.test");
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerPort()).thenReturn(443);
        when(mockOAuthProvider.getAuthorizationUrl("https://dotcms-local.test"))
                .thenReturn("https://dotcms-local.test/test-oauth-redirecturi");

        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(mockRequest);
        assertThat(useCaseResult.getRedirectUri()).isEqualTo("https://dotcms-local.test/test-oauth-redirecturi");
    }
}
