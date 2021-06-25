package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.usecases.FrontEndAuthenticationRequiredUseCase;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class FrontEndLoginRequiredInterceptor extends OAuthInterceptorBaseClass {

    private static final String NAME = "FrontEndLoginRequiredOAuthInterceptor_5_0_1";

    private final List<String> frontEndLoginUrls;
    private final FrontEndAuthenticationRequiredUseCase frontEndAuthenticationRequiredUseCase;

    @Override
    public String[] getFilters() {
        return (String[]) frontEndLoginUrls.toArray();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Result intercept(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        UseCaseResult useCaseResult = frontEndAuthenticationRequiredUseCase.determineIfAuthenticationIsRequired(request);
        return redirectOrContinueFilterChain(useCaseResult, response);
    }

}
