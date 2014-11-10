/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.security.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.fabric3.api.Role;
import org.fabric3.spi.security.AuthorizationService;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.NotAuthorizedException;

/**
 *
 */
public class AuthorizationServiceImplTestCase extends TestCase {

    public void testHasRole() throws Exception {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("role1"));
        roles.add(new Role("role2"));

        BasicSecuritySubject subject = new BasicSecuritySubject("foo", "bar", roles);

        AuthorizationService service = new AuthorizationServiceImpl();
        service.checkRole(subject, "role1");

        try {
            service.checkRole(subject, "role3");
            fail();
        } catch (NotAuthorizedException e) {
            // expected
        }
    }

    public void testHasRoles() throws Exception {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("role1"));
        roles.add(new Role("role2"));

        BasicSecuritySubject subject = new BasicSecuritySubject("foo", "bar", roles);

        List<String> subjectRoles = new ArrayList<>();
        subjectRoles.add("role1");
        subjectRoles.add("role2");
        AuthorizationService service = new AuthorizationServiceImpl();
        service.checkRoles(subject, subjectRoles);

        subjectRoles.add("role3");
        try {
            service.checkRoles(subject, subjectRoles);
            fail();
        } catch (NotAuthorizedException e) {
            // expected
        }
    }


}