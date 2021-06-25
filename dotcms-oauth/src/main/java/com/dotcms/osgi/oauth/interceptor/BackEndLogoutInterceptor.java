package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.usecases.BackEndLogoutUseCase;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class BackEndLogoutInterceptor extends OAuthInterceptorBaseClass {

    private static final String NAME = "BackEndLogoutInterceptor_5_0_1";

    private final BackEndLogoutUseCase backEndLogoutUseCase;
    private final List<String> backEndLogoutUrls;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getFilters() {
        return (String[]) backEndLogoutUrls.toArray();
    }

    @Override
    public Result intercept(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UseCaseResult result = backEndLogoutUseCase.logoutUser(request);
        setCacheInvalidationHeaders(response);
        return redirectOrContinueFilterChain(result, response);
    }
}