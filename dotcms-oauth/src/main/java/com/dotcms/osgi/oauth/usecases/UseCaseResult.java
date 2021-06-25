package com.dotcms.osgi.oauth.usecases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UseCaseResult {
    private final UseCaseResultEnum useCaseResultEnum;
    private final String redirectUri;

    public UseCaseResult(UseCaseResultEnum useCaseResultEnum) {
        this.useCaseResultEnum = useCaseResultEnum;
        this.redirectUri = "";
    }
}
