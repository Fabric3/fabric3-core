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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

import org.fabric3.api.SecuritySubject;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.ContainerException;
import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.ConfigurationParser;
import org.fabric3.security.spring.factory.AuthenticationProviderFactory;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthenticationToken;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Serves as a bridge between the Fabric3 security SPI and SpringSecurity for authentication by implementing {@link AuthenticationService} and
 * <code>AuthenticationManager</code> respectively. This allows Fabric3 code (e.g. transports) to authenticate a request using the Fabric3 security SPI and
 * Spring code to authenticate against the Spring Security API. <p/> Authentication is done using a set of Spring <code>AuthenticationProvider</code>s.
 */
@Service({AuthenticationManager.class, AuthenticationService.class})
public class Fabric3ProviderManager extends ProviderManager implements AuthenticationService {
    private AuthenticationProviderFactory factory;
    private ConfigurationParser parser;
    private AuthenticationManagerConfiguration configuration;
    private SecurityMonitor monitor;
    private boolean disabled;

    public Fabric3ProviderManager(@Reference ConfigurationParser parser, @Reference AuthenticationProviderFactory factory, @Monitor SecurityMonitor monitor) {
        this.parser = parser;
        this.factory = factory;
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setAuthenticationProviders(List<AuthenticationProvider> providers) {
        super.setProviders(providers);
    }

    @Property(required = false)
    public void setConfiguration(XMLStreamReader reader) throws XMLStreamException, ContainerException {
        configuration = parser.parse(reader);
    }

    @Override
    @Init
    public void afterPropertiesSet() throws Exception {
        if (configuration == null) {
            monitor.disabled();
            disabled = true;
            return;
        }
        setEraseCredentialsAfterAuthentication(configuration.isEraseCredentials());
        // instantiate providers
        List<AuthenticationProvider> providers = factory.create(configuration);
        setProviders(providers);
        super.afterPropertiesSet();
    }

    public SecuritySubject authenticate(AuthenticationToken<?, ?> token) throws AuthenticationException {
        if (disabled) {
            monitor.error("Attempt to authenticate when authentication is disabled");
            throw new AuthenticationException("Authentication is disabled");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL as the Sun JNDI LDAP provider implementation requires it
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Authentication authentication;
            if (token instanceof UsernamePasswordToken) {
                UsernamePasswordToken userToken = (UsernamePasswordToken) token;
                authentication = new UsernamePasswordAuthenticationToken(userToken.getPrincipal(), userToken.getCredentials());
            } else {
                // TODO support other tokens
                throw new UnsupportedOperationException("Support for token type not yet implemented");
            }
            authentication = authenticate(authentication);
            return new SpringSecuritySubject(authentication);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
