/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.security.AuthorizationException;
import org.fabric3.spi.security.AuthorizationService;

/**
 * Serves as a bridge between the Fabric3 security SPI and SpringSecurity for access control by implementing {@link AuthorizationService} and
 * <code>AccessDecisionManager</code> respectively. This allows Fabric3 code (e.g. transports) to check authorization using the Fabric3 security SPI
 * and Spring code to use the Spring Security API.
 * <p/>
 * Access decisions are delegated to a Spring <code>AccessDecisionManager<code> instance.
 */
@Service(names = {AccessDecisionManager.class, AuthorizationService.class})
public class Fabric3AccessDecisionManager extends AbstractAccessDecisionManager implements AuthorizationService {
    private String managerType = "affirmative";

    private AccessDecisionManager delegate;

    @Property(required = false)
    public void setManagerType(String managerType) {
        this.managerType = managerType;
    }

    @Reference(required = false)
    public void setDecisionVoters(List<AccessDecisionVoter> voters) {
        if (voters.isEmpty()) {
            return;
        }
        super.setDecisionVoters(voters);
    }

    @Init
    public void init() throws SecurityInitException {
        if (getDecisionVoters() == null || getDecisionVoters().isEmpty()) {
            List<AccessDecisionVoter> voters = new ArrayList<AccessDecisionVoter>();
            RoleVoter roleVoter = new RoleVoter();
            voters.add(roleVoter);
            AuthenticatedVoter authenticatedVoter = new AuthenticatedVoter();
            voters.add(authenticatedVoter);
            setDecisionVoters(voters);
        }

        if ("affirmative".equals(managerType)) {
            AffirmativeBased affirmativeBased = new AffirmativeBased();
            affirmativeBased.setDecisionVoters(getDecisionVoters());
            delegate = affirmativeBased;
        } else if ("consensus".equals(managerType)) {
            ConsensusBased consensusBased = new ConsensusBased();
            consensusBased.setDecisionVoters(getDecisionVoters());
            delegate = consensusBased;
        } else if ("unanimous".equals(managerType)) {
            UnanimousBased unanimousBased = new UnanimousBased();
            unanimousBased.setDecisionVoters(getDecisionVoters());
            delegate = unanimousBased;
        } else {
            throw new SecurityInitException("Unknown access decision manager type: " + managerType);
        }
    }

    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        delegate.decide(authentication, object, configAttributes);
    }

    public void checkRole(SecuritySubject subject, String role) throws AuthorizationException {
        Authentication authentication = subject.getDelegate(Authentication.class);
        SecurityConfig config = new SecurityConfig(role);
        List<ConfigAttribute> configs = new ArrayList<ConfigAttribute>();
        configs.add(config);
        delegate.decide(authentication, null, configs);
    }

    public void checkRoles(SecuritySubject subject, Collection<String> roles) throws AuthorizationException {
        Authentication authentication = subject.getDelegate(Authentication.class);
        List<ConfigAttribute> configs = new ArrayList<ConfigAttribute>(roles.size());
        for (String role : roles) {
            SecurityConfig config = new SecurityConfig(role);
            configs.add(config);
        }
        delegate.decide(authentication, null, configs);
    }

    public void checkPermission(SecuritySubject subject, String role) throws AuthorizationException {
        checkRole(subject, role);
    }

    public void checkPermissions(SecuritySubject subject, Collection<String> roles) throws AuthorizationException {
        checkRoles(subject, roles);
    }
}