/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
