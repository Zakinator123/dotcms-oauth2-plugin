package com.dotcms.osgi.oauth;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.provider.OAuthProviderConnectionDetails;
import com.dotcms.osgi.oauth.util.Utils;
import com.liferay.portal.model.User;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scribe.utils.OAuthEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.scribe.model.OAuthConstants.ACCESS_TOKEN;

public class SecureAuthIntegrationTest extends BaseIntegrationTest {

    // This file path is the location of the default test properties file within the OSGi bundle.
    private static final String DEFAULT_PLUGIN_CONFIGURATION_FILE_PATH = "/oauth-plugin-default-test-configuration.properties";

    private static final String EXTERNAL_CONFIGURATION_FILE_NAME = "oauth-plugin-external-test-configuration.properties";
    private static final String OAUTH_PROVIDER_CONFIGURATION_PROPERTIES_FILE_NAME = "secureauth-oauth-provider-test.properties";

    private static String mockTokenResponse;
    private static String mockUserInfoResponse;

    @BeforeAll
    static void setUpIntegrationTest() throws IOException {
        setUpMockWebServer();
        mockTokenResponse = Utils.getResourceFileAsString("fixtures/secure_auth_token_response.json");
        mockUserInfoResponse = Utils.getResourceFileAsString("fixtures/secure_auth_user_info_response.json");
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        super.setUp(
                DEFAULT_PLUGIN_CONFIGURATION_FILE_PATH,
                EXTERNAL_CONFIGURATION_FILE_NAME,
                OAUTH_PROVIDER_CONFIGURATION_PROPERTIES_FILE_NAME);
    }

    @Test
    void testSecureAuthLoginRequiredRedirect() throws IOException {
        HttpServletRequest adminLoginAttemptRequest = mock(HttpServletRequest.class);
        HttpServletResponse adminLoginAttemptResponse = mock(HttpServletResponse.class);

        setUpMockRequestBaseUrlInformation(adminLoginAttemptRequest);
        when(adminLoginAttemptRequest.getRequestURI()).thenReturn("dotAdmin");
        when(adminLoginAttemptRequest.getSession(true)).thenReturn(mock(HttpSession.class));
        when(mockDotCmsFacade.dotCmsBackEndUserIsLoggedIn(any())).thenReturn(false);

        Result loginRequiredResult = this.getBackEndLoginRequiredInterceptor().intercept(adminLoginAttemptRequest, adminLoginAttemptResponse);
        OAuthProviderConnectionDetails connectionDetails = getPluginConfiguration().getOAuthProviderConnectionDetails();
        String authorizationEndpoint = connectionDetails.getAuthorizationEndpoint();
        String clientIdParameter = String.format("client_id=%s&", connectionDetails.getClientId());
        String clientSecretParameter = String.format("client_secret=%s&", connectionDetails.getClientSecret());
        String redirectUriParameter = String.format("redirect_uri=%s&", OAuthEncoder.encode("https://dotcms-local.test" + connectionDetails.getCallbackPath()));
        String scopesParameter = String.format("scope=%s&", OAuthEncoder.encode(connectionDetails.getScopes()));
        String secureAuthRedirectUrl = authorizationEndpoint +
                "?" +
                clientIdParameter +
                clientSecretParameter +
                redirectUriParameter +
                scopesParameter +
                "response_type=code";

        assertEquals(loginRequiredResult, Result.SKIP_NO_CHAIN);
        verify(adminLoginAttemptResponse).sendRedirect(secureAuthRedirectUrl);
    }

    @Test
    void testSecureAuthLoginSuccessfulCallback() throws IOException, InterruptedException {
        MockResponse mockWebServerResponse = new MockResponse();

        HttpServletRequest oAuthCallbackRequest = mock(HttpServletRequest.class);
        HttpServletResponse oAuthCallbackResponse = mock(HttpServletResponse.class);
        HttpSession mockSession = mock(HttpSession.class);

        when(oAuthCallbackRequest.getParameter("code")).thenReturn("12345");
        setUpMockRequestBaseUrlInformation(oAuthCallbackRequest);
        when(oAuthCallbackRequest.getSession(anyBoolean())).thenReturn(mockSession);

        mockWebServer.enqueue(mockWebServerResponse.setBody(mockTokenResponse));
        mockWebServer.enqueue(mockWebServerResponse.setBody(mockUserInfoResponse));

        User mockDotCmsUser = mock(User.class);
        when(mockDotCmsFacade.getOrCreateBackEndUser(any())).thenReturn(mockDotCmsUser);
        when(mockDotCmsUser.isActive()).thenReturn(true);

        Result callBackResult = getCallbackInterceptor().intercept(oAuthCallbackRequest, oAuthCallbackResponse);

        assertEquals(callBackResult, Result.SKIP_NO_CHAIN);
        verify(mockDotCmsFacade).loginBackEndUser(mockDotCmsUser, oAuthCallbackRequest, oAuthCallbackResponse);
        verify(oAuthCallbackResponse).sendRedirect("/dotAdmin");

        mockWebServer.takeRequest();
        mockWebServer.takeRequest();
    }

    @Test
    void testSecureAuthLogout() throws IOException {
        HttpServletRequest logoutRequest = mock(HttpServletRequest.class);
        HttpServletResponse logoutResponse = mock(HttpServletResponse.class);
        HttpSession mockSession = mock(HttpSession.class);

        when(logoutRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute(ACCESS_TOKEN)).thenReturn("test-access-token");

        Result logoutResult = getBackEndLogoutInterceptor().intercept(logoutRequest, logoutResponse);
        verify(mockSession).removeAttribute(ACCESS_TOKEN);
        assertEquals(logoutResult, Result.NEXT);
    }
}
