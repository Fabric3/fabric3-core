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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.security.AuthorizationException;
import org.fabric3.spi.security.AuthorizationService;
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

/**
 * Serves as a bridge between the Fabric3 security SPI and SpringSecurity for access control by implementing {@link AuthorizationService} and
 * <code>AccessDecisionManager</code> respectively. This allows Fabric3 code (e.g. transports) to check authorization using the Fabric3 security SPI
 * and Spring code to use the Spring Security API.
 * <p/>
 * Access decisions are delegated to a Spring <code>AccessDecisionManager<code> instance.
 */
@Service({AccessDecisionManager.class, AuthorizationService.class})
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
    public void init() throws ContainerException {
        if (getDecisionVoters() == null || getDecisionVoters().isEmpty()) {
            List<AccessDecisionVoter> voters = new ArrayList<>();
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
            throw new ContainerException("Unknown access decision manager type: " + managerType);
        }
    }

    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        delegate.decide(authentication, object, configAttributes);
    }

    public void checkRole(SecuritySubject subject, String role) throws AuthorizationException {
        Authentication authentication = subject.getDelegate(Authentication.class);
        SecurityConfig config = new SecurityConfig(role);
        List<ConfigAttribute> configs = new ArrayList<>();
        configs.add(config);
        delegate.decide(authentication, null, configs);
    }

    public void checkRoles(SecuritySubject subject, Collection<String> roles) throws AuthorizationException {
        Authentication authentication = subject.getDelegate(Authentication.class);
        List<ConfigAttribute> configs = new ArrayList<>(roles.size());
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