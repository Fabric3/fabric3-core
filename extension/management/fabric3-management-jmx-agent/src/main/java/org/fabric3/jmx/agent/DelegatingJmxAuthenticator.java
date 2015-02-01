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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.jmx.agent;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.host.Fabric3Exception;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Delegates to a runtime extension to perform JMX authentication.
 */
@Service(DelegatingJmxAuthenticator.class)
public class DelegatingJmxAuthenticator implements JMXAuthenticator {
    private JmxSecurity security = JmxSecurity.DISABLED;
    private Set<Role> roles = new HashSet<>();
    private JMXAuthenticator delegate;

    @Property(required = false)
    public void setSecurity(String level) throws Fabric3Exception {
        try {
            security = JmxSecurity.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Fabric3Exception("Invalid JMX security setting:" + level);
        }
    }

    @Property(required = false)
    public void setRoles(String rolesAttribute) {
        String[] rolesString = rolesAttribute.split(",");
        for (String s : rolesString) {
            roles.add(new Role(s.trim()));
        }
    }

    /**
     * Used to obtain the JMXAuthenticator delegate when it becomes available as an extension. A collection is required since reinjection is only
     * performed on multiplicities.
     *
     * @param authenticators the authenticator
     */
    @Reference(required = false)
    public void setAuthenticators(List<JMXAuthenticator> authenticators) {
        if (authenticators.isEmpty()) {
            return;
        }
        delegate = authenticators.get(0);
    }

    public Subject authenticate(Object credentials) {
        if (delegate == null) {
            throw new SecurityException("Delegate JMXAuthenticator not configured");
        }
        Subject subject = delegate.authenticate(credentials);
        if (JmxSecurity.AUTHENTICATION == security) {
            // only perform authentication
            return subject;
        }
        if (authorize(subject)) {
            return subject;
        } else {
            throw new SecurityException("Access denied");
        }
    }

    private boolean authorize(Subject subject) {
        boolean authenticated = false;
        for (Role role : roles) {
            if (subject.getPrincipals().contains(role)) {
                authenticated = true;
                break;
            }
        }
        return authenticated;
    }
}