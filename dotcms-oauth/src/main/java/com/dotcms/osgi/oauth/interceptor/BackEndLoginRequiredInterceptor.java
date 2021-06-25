package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.usecases.BackEndAuthenticationRequiredUseCase;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import com.dotmarketing.util.Logger;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class BackEndLoginRequiredInterceptor extends OAuthInterceptorBaseClass {

    private static final String NAME = "BackEndLoginRequiredOAuthInterceptor_5_0_1";

    private final List<String> backEndLoginUrls;
    private final BackEndAuthenticationRequiredUseCase backEndAuthenticationRequiredUseCase;

    @Override
    public String[] getFilters() {
        return (String[]) backEndLoginUrls.toArray();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Result intercept(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        UseCaseResult useCaseResult = backEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(request);
        setCacheInvalidationHeaders(response);
        return redirectOrContinueFilterChain(useCaseResult, response);
    }
}
