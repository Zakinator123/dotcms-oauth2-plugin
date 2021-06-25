package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.osgi.oauth.usecases.FrontEndLogoutUseCase;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class FrontEndLogoutInterceptor extends OAuthInterceptorBaseClass {

    private static final String NAME = "FrontEndLogoutInterceptor_5_0_1";

    private final FrontEndLogoutUseCase frontEndLogoutUseCase;
    private final List<String> frontEndLogoutUrls;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getFilters() {
        return (String[]) frontEndLogoutUrls.toArray();
    }

    @Override
    public Result intercept(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UseCaseResult result = frontEndLogoutUseCase.logoutFrontEndUser(request);
        return redirectOrContinueFilterChain(result, response);
    }
}
