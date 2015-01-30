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
package org.fabric3.spi.security;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;

/**
 * SecuritySubject for the Fabric3 basic security implementation.
 */
public class BasicSecuritySubject implements SecuritySubject, Principal {
    private String username;
    private String password;
    private Set<Role> roles;
    private Subject jaasSubject;

    public BasicSecuritySubject(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        Set<Principal> principals = new HashSet<Principal>(roles);
        principals.add(this);
        jaasSubject = new Subject(true, principals, Collections.emptySet(), Collections.emptySet());

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean hasRole(String name) {
        return roles.contains(new Role(name));
    }

    public <T> T getDelegate(Class<T> type) {
        if (!BasicSecuritySubject.class.equals(type)) {
            throw new IllegalArgumentException("Unknown delegate type: " + type);
        }
        return type.cast(this);
    }

    public Subject getJaasSubject() {
        return jaasSubject;
    }

    public String getName() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}
