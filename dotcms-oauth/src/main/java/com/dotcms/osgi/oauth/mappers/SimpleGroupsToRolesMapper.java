package com.dotcms.osgi.oauth.mappers;

import java.util.List;

public class SimpleGroupsToRolesMapper implements GroupsToRolesMapper {

    @Override
    public List<String> mapUserGroupsToDotCmsRoles(List<String> groups) {
        return groups;
    }
}
