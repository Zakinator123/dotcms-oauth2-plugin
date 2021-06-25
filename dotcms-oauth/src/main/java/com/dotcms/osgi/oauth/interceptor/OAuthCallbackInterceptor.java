/*
 * WebSessionFilter
 *
 * A filter that recognizes return users who have chosen to have their login information remembered.
 * Creates a valid WebSession object and passes it a contact to use to fill its information
 *
 */
package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.usecases.AuthenticationUseCase;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import com.dotcms.osgi.oauth.usecases.UseCaseResultEnum;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dotcms.osgi.oauth.util.OAuthPluginConstants.AUTHORIZATION_CODE_PARAMETER;

@RequiredArgsConstructor
public class OAuthCallbackInterceptor extends OAuthInterceptorBaseClass {

    private static final String NAME = "AutoLoginOAuthInterceptor_5_0_1";

    private final String urlPathToIntercept;
    private final AuthenticationUseCase authenticationUseCase;

    @Override
    public String[] getFilters() {
        return new String[]{urlPathToIntercept.toLowerCase()};
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Result intercept(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UseCaseResult result = authenticationUseCase.authenticateWithDotCms(request, response);
        setCacheInvalidationHeaders(response);
        return redirectOrContinueFilterChain(result, response);
    }
}
