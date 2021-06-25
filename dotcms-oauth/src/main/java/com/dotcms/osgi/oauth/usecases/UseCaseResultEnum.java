package com.dotcms.osgi.oauth.usecases;

public enum UseCaseResultEnum {
    ALREADY_LOGGED_INTO_FRONT_END,
    ALREADY_LOGGED_INTO_BACK_END,
    ALREADY_LOGGED_OUT,
    FRONT_END_LOGOUT_SUCCESSFUL,
    BACK_END_LOGOUT_SUCCESSFUL,
    NO_AUTHENTICATION_REQUIRED,
    NATIVE_AUTHENTICATION_REQUIRED,
    SSO_AUTHENTICATION_REQUIRED,
    AUTHENTICATION_FAILURE,
    FRONT_END_AUTHENTICATION_SUCCESS,
    BACK_END_AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FORBIDDEN,
}
