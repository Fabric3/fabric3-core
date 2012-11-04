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

import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.fabric3.api.SecuritySubject;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.ConfigurationParser;
import org.fabric3.security.spring.config.SecurityConfigurationException;
import org.fabric3.security.spring.factory.AuthenticationProviderFactory;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthenticationToken;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 * Serves as a bridge between the Fabric3 security SPI and SpringSecurity for authentication by implementing {@link AuthenticationService} and
 * <code>AuthenticationManager</code> respectively. This allows Fabric3 code (e.g. transports) to authenticate a request using the Fabric3 security
 * SPI and Spring code to authenticate against the Spring Security API.
 * <p/>
 * Authentication is done using a set of Spring <code>AuthenticationProvider</code>s.
 */
@Service(names = {AuthenticationManager.class, AuthenticationService.class})
public class Fabric3ProviderManager extends ProviderManager implements AuthenticationService {
    private AuthenticationProviderFactory factory;
    private ConfigurationParser parser;
    private AuthenticationManagerConfiguration configuration;
    private SecurityMonitor monitor;
    private boolean disabled;

    public Fabric3ProviderManager(@Reference ConfigurationParser parser,
                                  @Reference AuthenticationProviderFactory factory,
                                  @Monitor SecurityMonitor monitor) {
        this.parser = parser;
        this.factory = factory;
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setAuthenticationProviders(List<AuthenticationProvider> providers) {
        super.setProviders(providers);
    }

    @Property(required = false)
    public void setConfiguration(XMLStreamReader reader) throws XMLStreamException, SecurityConfigurationException {
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
