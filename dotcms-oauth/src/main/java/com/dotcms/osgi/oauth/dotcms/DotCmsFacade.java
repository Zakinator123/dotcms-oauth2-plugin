package com.dotcms.osgi.oauth.dotcms;

import com.dotcms.osgi.oauth.provider.UserInfo;
import com.liferay.portal.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface DotCmsFacade {
    boolean dotCmsBackEndUserIsLoggedIn(HttpServletRequest request);

    User getOrCreateBackEndUser(UserInfo userInfo);

    void removeAllUserRoles(User user);

    void setUserRoles(User user, List<String> roleKeys);

    void loginBackEndUser(User user, HttpServletRequest request, HttpServletResponse response);
}
