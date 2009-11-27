/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.security.impl;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;

import org.fabric3.api.SecuritySubject;

/**
 * SecuritySubject for the Fabric3 basic security implementation.
 *
 * @version $Rev$ $Date$
 */
public class BasicSecuritySubject implements SecuritySubject {
    private String username;
    private String password;
    private List<Role> roles;
    private Map<String, Role> mapping = new HashMap<String, Role>();
    private Subject jaasSubject;

    public BasicSecuritySubject(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        for (Role role : roles) {
            mapping.put(role.getName(), role);
        }
        jaasSubject = new Subject(true, Collections.<Principal>emptySet(), Collections.emptySet(), Collections.emptySet());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public boolean hasRole(String name) {
        return mapping.containsKey(name);
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
}
