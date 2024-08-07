/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations
 * (FAO). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.cs.services.ejbs.admin.businesslogic;

import java.util.List;
import org.apache.ibatis.session.Configuration;
import org.sola.services.common.EntityTable;
import org.sola.services.common.ejbs.AbstractEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.Role;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.Group;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.GroupSummary;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.Language;

/**
 * Local interface for the {@linkplain AdminEJB}.
 */
public interface AdminCSEJBLocal extends AbstractEJBLocal {
    static final String FOUND = ">>> Found ";
    static final String FAILED_GET_GROUP = "Failed to get group";

    /**
     * See {@linkplain AdminEJB#getUsers()
     * AdminEJB.getUsers}
     */
    List<User> getUsers();

    /**
     * See {@linkplain AdminEJB#getDbConfiguration()}
     */
    Configuration getDbConfiguration();

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getUser(java.lang.String)
     * AdminEJB.getUser}
     *
     * @param userName
     * @return
     */
    User getUser(String userName);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#isUserNameExists(java.lang.String)}
     *
     * @param userName
     * @return
     */
    boolean isUserNameExists(String userName);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#isUserEmailExists(java.lang.String)}
     *
     * @param email
     * @return
     */
    boolean isUserEmailExists(String email);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#isUserEmailExists(java.lang.String, java.lang.String)}
     *
     * @param email
     * @param exludeUserName
     * @return
     */
    boolean isUserEmailExists(String email, String exludeUserName);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#checkCurrentUserPassword(java.lang.String)}
     *
     * @param password
     * @return
     */
    boolean checkCurrentUserPassword(String password);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#checkCurrentUserPassword(java.lang.String)}
     *
     * @param password
     * @return
     */
    boolean changeCurrentUserPassword(String password);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#isUserActive(java.lang.String)}
     */
    boolean isUserActive(String userName);

    /**
     * See
     * {@link org.sola.services.ejbs.admin.businesslogic.AdminEJB#isUserActiveByEmail(java.lang.String)}
     */
    boolean isUserActiveByEmail(String email);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getCurrentUser()
     * AdminEJB.getCurrentUser}
     */
    User getCurrentUser();

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getUserFullName(java.lang.String)
     */
    String getUserFullName(String userName);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#saveCurrentUser(org.sola.services.ejbs.admin.businesslogic.repository.entities.User)
     * AdminEJB.saveUser}
     */
    User saveCurrentUser(User user);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#createCommunityRecorderUser(org.sola.services.ejbs.admin.businesslogic.repository.entities.User)
     * AdminCSEJB.createCommunityUser}
     */
    User createCommunityUser(User user);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#changePassword(java.lang.String, java.lang.String)
     * AdminEJB.changePassword}
     */
    boolean changePassword(String userName, String password);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#changePasswordByRestoreCode(java.lang.String, java.lang.String)
     * AdminEJB.changePassword}
     */
    boolean changePasswordByRestoreCode(String restoreCode, String password);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getRoles()
     * AdminEJB.getRoles}
     */
    List<Role> getRoles();

    /**
     * See {@linkplain AdminEJB#getUserRoles(java.lang.String)
     * AdminEJB.getUserRoles}
     */
    List<Role> getUserRoles(String userName);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getCurrentUserRoles()
     * AdminEJB.getCurrentUserRoles}
     */
    List<Role> getCurrentUserRoles();

    /**
     * See {@linkplain AdminEJB#getRole(java.lang.String)
     * AdminEJB.getRole}
     */
    Role getRole(String roleCode);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getGroups()
     * AdminEJB.getGroups}
     */
    List<Group> getGroups();

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getGroupsSummary()
     * AdminEJB.getGroupsSummary}
     */
    List<GroupSummary> getGroupsSummary();

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#getGroup(java.lang.String)
     * AdminEJB.getGroup}
     */
    Group getGroup(String groupId);

    /**
     * See {@linkplain org.sola.services.ejbs.admin.businesslogic.AdminEJB#isUserAdmin()
     * AdminEJB.isUserAdmin}
     */
    boolean isUserAdmin();

    /**
     * See {@linkplain AdminEJB#getLanguages(java.lang.String)
     * AdminEJB.getLanguages}
     */
    List<Language> getLanguages(String lang);

    /**
     * See {@linkplain AdminCSEJB#restoreUserPassword(String, String)}
     */
    void restoreUserPassword(String email, String projectId);

    /**
     * See {@linkplain AdminEJB#getUserByActivationCode(String)}
     */
    User getUserByActivationCode(String activationCode);

    /**
     * See {@linkplain AdminEJB#getUserInfo(String)}
     */
    User getUserInfo(String userName);

    /**
     * See {@linkplain AdminEJB#flushCache()}
     */
    void flushCache();
}
