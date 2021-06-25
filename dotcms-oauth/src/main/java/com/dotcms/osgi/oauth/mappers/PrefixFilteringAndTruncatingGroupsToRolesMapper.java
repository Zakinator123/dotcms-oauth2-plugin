package com.dotcms.osgi.oauth.mappers;

import com.dotmarketing.util.Logger;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PrefixFilteringAndTruncatingGroupsToRolesMapper implements GroupsToRolesMapper {

    private final List<String> prefixes;

    @Override
    public List<String> mapUserGroupsToDotCmsRoles(List<String> groups) {
        Logger.info(this, "Mapping Groups from the Identity Provider into DotCMS Roles. The groups from the Identity Provider are: " + groups.toString());
        List<String> filteredGroups = groups.stream().filter(this::groupContainsValidPrefix).collect(Collectors.toList());
        List<String> dotCmsRoles = filteredGroups.stream().map(this::truncatedGroup).collect(Collectors.toList());
        Logger.info(this, "The resulting mapped DotCMS Roles are: " + dotCmsRoles);
        return dotCmsRoles;
    }

    private boolean groupContainsValidPrefix(String group) {
        for (String prefix : prefixes) {
            if (group.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String truncatedGroup(String group) {
        for (String prefix : prefixes) {
            if (group.contains(prefix)) {
                return group.substring(prefix.length());
            }
        }
        Logger.error(this, String.format("No prefixes could be truncated from the following group: %s", group));
        return group;
    }
}
