package com.dotcms.osgi.oauth.dotcms;

import com.dotcms.cms.login.LoginServiceAPI;
import com.dotcms.enterprise.PasswordFactoryProxy;
import com.dotcms.enterprise.de.qaware.heimdall.PasswordException;
import com.dotcms.osgi.oauth.exceptions.UserCreationException;
import com.dotcms.osgi.oauth.exceptions.UserRoleDoesNotExistException;
import com.dotcms.osgi.oauth.exceptions.UserRoleUpdateException;
import com.dotcms.osgi.oauth.provider.UserInfo;
import com.dotcms.util.security.EncryptorFactory;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.Role;
import com.dotmarketing.business.RoleAPI;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;
import com.liferay.portal.auth.PrincipalThreadLocal;
import com.liferay.portal.model.User;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class DotCmsService implements DotCmsFacade {

    private final UserAPI userApi;
    private final RoleAPI rolesApi;
    private final LoginServiceAPI loginApi;

    @Override
    public boolean dotCmsBackEndUserIsLoggedIn(HttpServletRequest request) {
        return loginApi.isLoggedIn(request);
    }

    private User getSystemUser() {
        try {
            return userApi.getSystemUser();
        } catch (Exception e) {
            throw new UserCreationException(e);
        }
    }

    @Override
    public User getOrCreateBackEndUser(UserInfo userInfo) {
        User backEndUser;
        try {
            backEndUser = userApi.loadByUserByEmail(userInfo.getEmail(), getSystemUser(), false);
            Logger.info(this, "User with email address " + backEndUser.getEmailAddress() + " already exists in DotCMS.");
        } catch (Exception e) {
            Logger.info(this, "No matching user found, creating user..");
            backEndUser = createDotCmsBackEndUser(userInfo);
        }
        return backEndUser;
    }


    private User createDotCmsBackEndUser(UserInfo userInfo) {
        try {
            final String userId = UUIDGenerator.generateUuid();
            User user = userApi.createUser(userId, userInfo.getEmail());
            user.setNickName(userInfo.getFirstName());
            user.setFirstName(userInfo.getFirstName());
            user.setLastName(userInfo.getLastName());
            user.setActive(true);
            user.setCreateDate(new Date());
            user.setPassword(
                    PasswordFactoryProxy.generateHash(
                            UUIDGenerator.generateUuid()
                                    + "/"
                                    + UUIDGenerator.generateUuid()
                    ));

            user.setPasswordEncrypted(true);

            userApi.save(user, getSystemUser(), false);
            return user;

        } catch (DotDataException | PasswordException | DotSecurityException e) {
            e.printStackTrace();
            throw new UserCreationException(e);
        }
    }

    @Override
    public void removeAllUserRoles(User user) {
        try {
            rolesApi.removeAllRolesFromUser(user);
        } catch (DotDataException e) {
            throw new UserRoleUpdateException("An error occured while clearing old roles from user.", e);
        }
    }

    @Override
    public void setUserRoles(User user, List<String> roleKeys) {
        roleKeys.forEach(roleKey -> addRoleToUser(roleKey, user));
    }

    private void addRoleToUser(String roleKey, User user) {

        if (roleKey.isEmpty()) return;

        try {
            Role role = getRoleIfExists(roleKey);
            if (!rolesApi.doesUserHaveRole(user, role)) {
                Logger.info(this, "Adding the following role to the user: " + roleKey);
                rolesApi.addRoleToUser(role, user);
            }
        } catch (DotDataException | DotStateException | UserRoleDoesNotExistException e) {
            Logger.error(this, "An error occured while attempting to assign the following role to the user: " + roleKey, e);
        }
    }

    private Role getRoleIfExists(String roleKey) throws DotDataException, UserRoleDoesNotExistException {
        Role role = rolesApi.loadRoleByKey(roleKey);

        if (role == null) {
            throw new UserRoleDoesNotExistException("This role does not already exist in dotCMS: " + roleKey);
        }

        return role;
    }

    @Override
    public void loginBackEndUser(User user, HttpServletRequest request, HttpServletResponse response) {

        boolean loginResult = loginApi.doCookieLogin(EncryptorFactory.getInstance().getEncryptor().encryptString(user.getUserId()), request, response, false);
        Logger.info(this, "The DotCMS cookie login result was successful: " + loginResult);

        // doCookieLogin invalidates the previous session (and copies over the old attributes into a new session)
        // so the session must be re-fetched here.
        HttpSession session = request.getSession();

        PrincipalThreadLocal.setName(user.getUserId());
        session.setAttribute(com.liferay.portal.util.WebKeys.USER_ID, user.getUserId());
        session.setAttribute(com.liferay.portal.util.WebKeys.USER, user);
    }
}
