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
package org.fabric3.security.spring.config;

import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Parses a Spring Security configuration.
 */
public class ConfigurationParserImpl implements ConfigurationParser {
    private static final Set<String> PASSWORD_ENCODERS = new HashSet<>();

    static {
        PASSWORD_ENCODERS.add("plaintext");
        PASSWORD_ENCODERS.add("sha");
        PASSWORD_ENCODERS.add("sha-256");
        PASSWORD_ENCODERS.add("md4");
        PASSWORD_ENCODERS.add("md5");
    }

    public AuthenticationManagerConfiguration parse(XMLStreamReader reader) throws XMLStreamException, SecurityConfigurationException {
        reader.nextTag();
        AuthenticationManagerConfiguration managerConfiguration = new AuthenticationManagerConfiguration();
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("authentication-manager".equals(reader.getName().getLocalPart())) {
                    String alias = reader.getAttributeValue(null, "alias");
                    boolean erase = Boolean.parseBoolean(reader.getAttributeValue(null, "erase-credentials "));
                    managerConfiguration.setAlias(alias);
                    managerConfiguration.setEraseCredentials(erase);
                } else if ("http".equals(reader.getName().getLocalPart())) {
                    raiseConfigurationException("The <http> element is not supported for runtime-level configuration. " +
                            "It must be configured in the Spring application context", reader);
                } else if ("ldap-server".equals(reader.getName().getLocalPart())) {
                    LdapServerConfiguration serverConfiguration = parseLdapServer(reader);
                    managerConfiguration.setLdapServerConfiguration(serverConfiguration);
                } else if ("authentication-provider".equals(reader.getName().getLocalPart())) {
                    AuthenticationProviderConfiguration providerConfiguration = parseProvider(reader);
                    managerConfiguration.add(providerConfiguration);
                } else if ("ldap-authentication-provider".equals(reader.getName().getLocalPart())) {
                    LdapProviderConfiguration providerConfiguration = parseLdapProvider(reader);
                    managerConfiguration.add(providerConfiguration);
                }

                break;
            case XMLStreamConstants.END_DOCUMENT:
                if (managerConfiguration.getProviderConfigurations().isEmpty()) {
                    throw new SecurityConfigurationException("An authentication provider must be configured for the authentication manager");
                }
                // TODO validate alias on manager
                return managerConfiguration;
            }
        }
    }

    private LdapProviderConfiguration parseLdapProvider(XMLStreamReader reader) {
        LdapProviderConfiguration configuration = new LdapProviderConfiguration();
        String entry = reader.getAttributeValue(null, "user-dn-pattern");
        if (entry != null) {
            configuration.setDnPattern(new String[]{entry});
        }
        configuration.setGroupSearchBase(reader.getAttributeValue(null, "group-search-base"));
        String groupSearchFilter = reader.getAttributeValue(null, "group-search-filter");
        if (groupSearchFilter != null) {
            configuration.setGroupSearchFilter(groupSearchFilter);
        }
        String groupRoleAttribute = reader.getAttributeValue(null, "group-role-attribute");
        if (groupRoleAttribute != null) {
            configuration.setGroupRoleAttribute(groupRoleAttribute);
        }
        configuration.setUserSearchBase(reader.getAttributeValue(null, "user-search-base"));
        configuration.setUserSearchFilter(reader.getAttributeValue(null, "user-search-filter"));
        return configuration;
    }

    private AuthenticationProviderConfiguration parseProvider(XMLStreamReader reader) throws XMLStreamException, SecurityConfigurationException {
        String passwordEncoder = null;
        boolean base64 = false;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("user-service".equals(reader.getName().getLocalPart())) {
                    // TODO support in-memory user service
                    raiseConfigurationException("The generic <user-service> element is not yet supported", reader);
                } else if ("jdbc-user-service".equals(reader.getName().getLocalPart())) {
                    String dataSource = reader.getAttributeValue(null, "data-source-ref");
                    if (dataSource == null) {
                        raiseConfigurationException("A datasource must be specified on the <jdbc-user-service> element", reader);
                    }
                    JdbcProviderConfiguration configuration = new JdbcProviderConfiguration(dataSource);
                    configuration.setPasswordEncoder(passwordEncoder);
                    configuration.setUseBase64(base64);
                    return configuration;
                } else if ("password-encoder".equals(reader.getName().getLocalPart())) {
                    passwordEncoder = reader.getAttributeValue(null, "hash");
                    if (passwordEncoder == null) {
                        throw new SecurityConfigurationException("A hash vale must be configured for the password encoder");
                    }
                    if (!PASSWORD_ENCODERS.contains(passwordEncoder)) {
                        throw new SecurityConfigurationException("Invalid password encoder value: " + passwordEncoder);
                    }
                    base64 = Boolean.parseBoolean(reader.getAttributeValue(null, "useBase64"));
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("authentication-provider".equals(reader.getName().getLocalPart())) {
                    raiseConfigurationException("Authentication provider configuration must define at least one provider", reader);
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                throw new AssertionError("End of document encountered");

            }
        }
    }

    private LdapServerConfiguration parseLdapServer(XMLStreamReader reader) throws SecurityConfigurationException {
        String url = reader.getAttributeValue(null, "url");
        if (url == null) {
            raiseConfigurationException("LDAP server configuration must specify a URL", reader);
        }
        LdapServerConfiguration configuration = new LdapServerConfiguration(url);
        String managerDN = reader.getAttributeValue(null, "manager-dn");
        if (managerDN != null) {
            configuration.setManagerDN(managerDN);
        }
        String managerPassword = reader.getAttributeValue(null, "manager-password");
        if (managerPassword != null) {
            configuration.setManagerPassword(managerPassword);
        }
        return configuration;
    }

    private void raiseConfigurationException(String message, XMLStreamReader reader) throws SecurityConfigurationException {
        Location location = reader.getLocation();
        if (location == null) {
            throw new SecurityConfigurationException(message);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        throw new SecurityConfigurationException(message + " [" + line + "," + col + "]");
    }

}