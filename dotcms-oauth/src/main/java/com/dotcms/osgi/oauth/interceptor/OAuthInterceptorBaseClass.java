package com.dotcms.osgi.oauth.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotcms.osgi.oauth.usecases.UseCaseResult;
import com.dotmarketing.util.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuthInterceptorBaseClass implements WebInterceptor {

    Result redirectOrContinueFilterChain(UseCaseResult useCaseResult, HttpServletResponse response) throws IOException {

        Logger.info(this, "User operation resulted in " + useCaseResult.getUseCaseResultEnum());

        switch (useCaseResult.getUseCaseResultEnum()) {
            case SSO_AUTHENTICATION_REQUIRED:
            case AUTHENTICATION_FAILURE:
            case FRONT_END_AUTHENTICATION_SUCCESS:
            case BACK_END_AUTHENTICATION_SUCCESS:
            case AUTHENTICATION_FORBIDDEN:
            case ALREADY_LOGGED_INTO_FRONT_END:
            case FRONT_END_LOGOUT_SUCCESSFUL:
                String redirectUri = useCaseResult.getRedirectUri();
                if (redirectUri.isEmpty()) {
                    redirectUri = "/";
                }
                Logger.info(this, "Redirecting user to '" + useCaseResult.getRedirectUri() + "'");
                response.sendRedirect(redirectUri);
                return Result.SKIP_NO_CHAIN;
            case ALREADY_LOGGED_INTO_BACK_END:
            case NATIVE_AUTHENTICATION_REQUIRED:
            case NO_AUTHENTICATION_REQUIRED:
            case BACK_END_LOGOUT_SUCCESSFUL:
            case ALREADY_LOGGED_OUT:
            default:
                return Result.NEXT;
        }
    }

    @Override
    public Result intercept(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        return null;
    }

    void setCacheInvalidationHeaders(HttpServletResponse response) {
        Logger.info(this, "Setting cache invalidation headers.");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        }
}
