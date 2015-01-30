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
package org.fabric3.security.spring.factory;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.AuthenticationProviderConfiguration;
import org.fabric3.security.spring.config.JdbcProviderConfiguration;
import org.fabric3.security.spring.config.LdapProviderConfiguration;
import org.fabric3.security.spring.config.LdapServerConfiguration;
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

/**
 *
 */
public class AuthenticationProviderFactoryImpl implements AuthenticationProviderFactory {
    private DataSourceRegistry registry;

    @Reference
    public void setRegistry(DataSourceRegistry registry) {
        this.registry = registry;
    }

    public List<AuthenticationProvider> create(AuthenticationManagerConfiguration configuration) {
        List<AuthenticationProvider> providers = new ArrayList<>();
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