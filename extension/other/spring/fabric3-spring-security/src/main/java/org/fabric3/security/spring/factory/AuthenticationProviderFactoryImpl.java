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
package org.fabric3.security.spring.factory;

import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.Md4PasswordEncoder;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.AuthenticationProviderConfiguration;
import org.fabric3.security.spring.config.JdbcProviderConfiguration;
import org.fabric3.security.spring.config.LdapProviderConfiguration;
import org.fabric3.security.spring.config.LdapServerConfiguration;

/**
 * @version $Rev$ $Date$
 */
public class AuthenticationProviderFactoryImpl implements AuthenticationProviderFactory {
    private DataSourceRegistry registry;

    @Reference
    public void setRegistry(DataSourceRegistry registry) {
        this.registry = registry;
    }

    public List<AuthenticationProvider> create(AuthenticationManagerConfiguration configuration) {
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
        BaseLdapPathContextSource contextSource = null;
        LdapServerConfiguration ldapServerConfiguration = configuration.getLdapServerConfiguration();
        if (ldapServerConfiguration != null) {
            contextSource = createContextSource(ldapServerConfiguration);
        }

        for (AuthenticationProviderConfiguration providerConfiguration : configuration.getProviderConfigurations()) {

            if (providerConfiguration instanceof JdbcProviderConfiguration) {
                AuthenticationProvider provider = createJdbcProvider(providerConfiguration);
                providers.add(provider);
            } else if (providerConfiguration instanceof LdapProviderConfiguration) {
                AuthenticationProvider provider = createLdapProvider(contextSource, (LdapProviderConfiguration) providerConfiguration);
                providers.add(provider);
            }
        }

        return providers;
    }

    private AuthenticationProvider createLdapProvider(BaseLdapPathContextSource contextSource, LdapProviderConfiguration ldapConfiguration) {
        LdapAuthenticator authenticator = createAuthenticator(ldapConfiguration, contextSource);
        LdapAuthoritiesPopulator populator = createPopulator(contextSource, ldapConfiguration);
        return new LdapAuthenticationProvider(authenticator, populator);
    }

    private AuthenticationProvider createJdbcProvider(AuthenticationProviderConfiguration configuration) {
        JdbcProviderConfiguration jdbcConfiguration = (JdbcProviderConfiguration) configuration;
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        PasswordEncoder encoder = createPasswordEncoder(jdbcConfiguration);
        if (encoder != null) {
            provider.setPasswordEncoder(encoder);
        }
        JdbcDaoImpl userService = new JdbcDaoImpl();
        String dsName = jdbcConfiguration.getDataSourceName();
        DataSourceWrapper dataSource = new DataSourceWrapper(dsName, registry);
        userService.setDataSource(dataSource);
        // TODO configure SQL
        provider.setUserDetailsService(userService);
        return provider;
    }

    private PasswordEncoder createPasswordEncoder(JdbcProviderConfiguration configuration) {
        String encoderType = configuration.getPasswordEncoder();
        if (encoderType == null) {
            return null;
        }
        boolean base64 = configuration.isUseBase64();
        if ("plaintext".equals(encoderType)) {
            return new PlaintextPasswordEncoder();
        } else if ("sha".equals(encoderType)) {
            ShaPasswordEncoder encoder = new ShaPasswordEncoder();
            encoder.setEncodeHashAsBase64(base64);
            return encoder;
        } else if ("sha-256".equals(encoderType)) {
            ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
            encoder.setEncodeHashAsBase64(base64);
            return encoder;
        } else if ("md4".equals(encoderType)) {
            Md4PasswordEncoder encoder = new Md4PasswordEncoder();
            encoder.setEncodeHashAsBase64(base64);
            return encoder;
        } else if ("md5".equals(encoderType)) {
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            encoder.setEncodeHashAsBase64(base64);
            return encoder;
        }
        throw new AssertionError("Unknown encoder type:" + encoderType);
    }

    private LdapAuthenticator createAuthenticator(LdapProviderConfiguration ldapConfiguration, BaseLdapPathContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        String[] pattern = ldapConfiguration.getDnPattern();
        if (pattern != null) {
            authenticator.setUserDnPatterns(pattern);
        } else {
            String userSearchBase = ldapConfiguration.getUserSearchBase();
            String userSearchFilter = ldapConfiguration.getUserSearchFilter();
            LdapUserSearch userSearch = new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter, contextSource);
            authenticator.setUserSearch(userSearch);
        }
        return authenticator;
    }

    private LdapAuthoritiesPopulator createPopulator(BaseLdapPathContextSource contextSource, LdapProviderConfiguration configuration) {
        String groupSearchBase = configuration.getGroupSearchBase();
        String groupSearchFilter = configuration.getGroupSearchFilter();
        String groupRoleAttribute = configuration.getGroupRoleAttribute();
        DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
        populator.setGroupSearchFilter(groupSearchFilter);
        populator.setGroupRoleAttribute(groupRoleAttribute);
        return populator;
    }

    private BaseLdapPathContextSource createContextSource(LdapServerConfiguration serverConfiguration) {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(serverConfiguration.getServerLocation());
        contextSource.setUserDn(serverConfiguration.getManagerDN());
        contextSource.setPassword(serverConfiguration.getManagerPassword());
        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            // ok to catch and swallow as checked exceptions are not thrown in the implementation
            e.printStackTrace();
        }
        return contextSource;
    }
}