package com.dotcms.osgi.oauth.provider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoClaimKeys {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String userId;
    private final String groups;
}
