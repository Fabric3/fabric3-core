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