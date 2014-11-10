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
package org.fabric3.security.spring;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.Subject;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;

/**
 * Implementation of {@link SecuritySubject} that wraps a Spring <code>Authentication</code> instance.
 */
public class SpringSecuritySubject implements SecuritySubject, Principal {
    private Authentication authentication;
    private Subject jaasSubject;
    private Set<Role> roles;

    public SpringSecuritySubject(Authentication authentication) {
        this.authentication = authentication;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        roles = new HashSet<>();
        Set<Principal> principals = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            Role role = new Role(authority.getAuthority());
            principals.add(role);
            roles.add(role);
        }
        principals.add(this);
        jaasSubject = new Subject(true, principals, Collections.emptySet(), Collections.emptySet());
    }

    public <T> T getDelegate(Class<T> type) {
        if (Authentication.class.equals(type)) {
            return type.cast(authentication);
        }
        return null;
    }

    public Subject getJaasSubject() {
        return jaasSubject;
    }

    public String getUsername() {
        return getName();
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getName() {
        return authentication.getName();
    }
}
