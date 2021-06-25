package com.dotcms.osgi.oauth.mappers;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PrefixFilteringGroupsToRolesMapper implements GroupsToRolesMapper {

    private final List<String> prefixes;

    @Override
    public List<String> mapUserGroupsToDotCmsRoles(List<String> groups) {
        return groups.stream().filter(this::groupContainsValidPrefix).collect(Collectors.toList());
    }

    private boolean groupContainsValidPrefix(String group) {
        for (String prefix : prefixes) {
            if (group.contains(prefix)) {
                return true;
            }
        }
        return false;
    }
}
