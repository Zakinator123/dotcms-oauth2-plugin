package com.dotcms.osgi.oauth.configuration;

import com.dotcms.osgi.oauth.exceptions.ConfigurationException;
import com.dotcms.osgi.oauth.mappers.PrefixFilteringGroupsToRolesMapper;
import com.dotcms.osgi.oauth.mappers.SimpleGroupsToRolesMapper;
import com.dotcms.osgi.oauth.mappers.GroupsToRolesMapper;
import com.dotcms.osgi.oauth.mappers.PrefixFilteringAndTruncatingGroupsToRolesMapper;
import com.dotmarketing.util.Logger;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class GroupsToRolesMapperFactory {

    private final String groupsToRolesMappingStrategy;
    private final List<String> prefixFilterList;

    GroupsToRolesMapper getGroupsToRolesMapper() {

        switch (groupsToRolesMappingStrategy) {
            case "SIMPLE":
                return new SimpleGroupsToRolesMapper();
            case "FILTER_AND_TRUNCATE_USING_PREFIX_LIST":
                Logger.info(this, "Creating a groups-to-roles mapper that will both filter IdP user" +
                        " groups based on prefixes and truncate the same prefixes off those groups. The prefix list" +
                        " that will be used is: " + prefixFilterList);
                return new PrefixFilteringAndTruncatingGroupsToRolesMapper(prefixFilterList);
            case "FILTER_USING_PREFIX_LIST":
                Logger.info(this, "Creating a groups-to-roles mapper that will filter IdP user" +
                        " groups based on prefixes. The prefix list" +
                        " that will be used is: " + prefixFilterList);
                return new PrefixFilteringGroupsToRolesMapper(prefixFilterList);
            default:
                throw new ConfigurationException("Invalid groups to roles mapping strategy: " + groupsToRolesMappingStrategy);

        }
    }
}
