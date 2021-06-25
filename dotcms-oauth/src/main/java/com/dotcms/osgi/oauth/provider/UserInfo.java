package com.dotcms.osgi.oauth.provider;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfo {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String userId;
    private final List<String> userGroups;
}
