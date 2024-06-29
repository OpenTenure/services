/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.cs.services.ejbs.admin;

import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.Language;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.UserGroup;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.UserTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sola.services.common.EntityAction;
import org.sola.services.common.test.AbstractEJBTest;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJB;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.Role;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.Group;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.GroupRole;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.GroupSummary;
import static org.junit.Assert.*;

public class AdminCSEJBIT extends AbstractEJBTest {

    public AdminCSEJBIT() {
        super();
    }
    private static final String FOUND = ">>> Found ";
    private static final String GROUP_FAILED = "Failed to get group";
    private static final String USER_ID = "tester-user-id";
    private static final String USER_NAME = "tester-user-name";
    private static final String GROUP_ID = "tester-group-id";
    private static final String LANG = "en";
    private static final String LOGIN_USER = "test";
    private static final String LOGIN_PASS = "test";

    @Before
    public void setUp() throws Exception {
        login(LOGIN_USER, LOGIN_PASS);
    }

    @After
    public void tearDown() throws Exception {
        logout();
    }

    /**
     * Test loading languages
     */
    @Test
    public void testLoadLanguages() throws Exception {
        System.out.println(">>> Loading all languages.");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            List<Language> result = instance.getLanguages(LANG);
            tx.commit();

            assertNotNull("List of languages is null.", result);
            System.out.println(FOUND + result.size() + " languages.");
            
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test roles loading
     */
    @Test
    public void testLoadAllRoles() throws Exception {
        System.out.println(">>> Loading all roles");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            List<Role> result = instance.getRoles();
            tx.commit();

            assertNotNull("List of roles is null.", result);
            System.out.println(FOUND + result.size() + " roles.");
            
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test getting current user roles
     */
    @Test
    public void testGetCurrentUserRoles() throws Exception {
        System.out.println(">>> Loading roles for current user.");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            List<Role> result = instance.getCurrentUserRoles();
            tx.commit();

            assertNotNull("List of roles for current user is null.", result);
            System.out.println(FOUND + result.size() + " roles for current user.");
            
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test if user has admin rights
     */
    @Test
    public void testIsUserAdmin() throws Exception {
        System.out.println(">>> Checking if user has admin rights.");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            boolean result = instance.isUserAdmin();
            tx.commit();
            
            if(result){
                System.out.println(">>> Current user has admin rights.");
            }else{
                System.out.println(">>> Current user doesn't have admin rights.");
            }
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }

    /**
     * Test password change
     */
    @Ignore
    @Test
    public void testPasswordChange() throws Exception {
        System.out.println(">>> Changing password");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            instance.changePassword("usr", "test");
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }

    /**
     * Test groups loading
     */
    @Test
    public void testLoadAllGroups() throws Exception {
        System.out.println(">>> Loading all groups");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            List<Group> result = instance.getGroups();
            tx.commit();

            assertNotNull("List of groups is null.", result);
            assertFalse("List of groups is empty.", result.size()<=0);
            
            System.out.println(FOUND + result.size() + " groups.");
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test groups summary loading
     */
    @Test
    public void testLoadAllGroupsSummary() throws Exception {
        System.out.println(">>> Loading all groups summary");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            List<GroupSummary> result = instance.getGroupsSummary();
            tx.commit();

            assertNotNull("List of groups summary is null.", result);
            assertFalse("List of groups summary is empty.", result.size()<=0);
            
            System.out.println(FOUND + result.size() + " groups summaries.");
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }
   
    /**
     * Test get all users.
     */
    @Test
    public void testGetUsers() throws Exception {
        System.out.println(">>> Getting all users.");
        UserTransaction tx = getUserTransaction();
        try {
            AdminCSEJBLocal instance = (AdminCSEJBLocal) getEJBInstance(AdminCSEJB.class.getSimpleName());
            tx.begin();
            List<User> users = instance.getUsers();
            tx.commit();
            assertNotNull("Failed to get users", users);
            
            System.out.println(FOUND + users.size() + " users!");
            
        } catch (Exception e) {
            tx.rollback();
            fail(e.getMessage());
        }
    }
}
