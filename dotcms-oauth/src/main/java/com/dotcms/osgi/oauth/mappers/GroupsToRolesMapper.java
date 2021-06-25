package com.dotcms.osgi.oauth.mappers;

import java.util.List;

public interface GroupsToRolesMapper {
    List<String> mapUserGroupsToDotCmsRoles(List<String> groups);
}
