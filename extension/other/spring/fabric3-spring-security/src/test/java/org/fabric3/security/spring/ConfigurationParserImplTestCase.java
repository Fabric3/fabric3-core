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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.AuthenticationProviderConfiguration;
import org.fabric3.security.spring.config.ConfigurationParserImpl;
import org.fabric3.security.spring.config.JdbcProviderConfiguration;
import org.fabric3.security.spring.config.LdapProviderConfiguration;
import org.fabric3.security.spring.config.LdapServerConfiguration;

/**
 *
 */
public class ConfigurationParserImplTestCase extends TestCase {
    private static final String JDBC_XML = "<authentication-manager>"
            + "  <authentication-provider>"
            + "      <jdbc-user-service data-source-ref='someDataSource'/>"
            + "  </authentication-provider>"
            + "</authentication-manager>";

    private static final String LDAP_USERDN_XML = "<authentication-manager>"
            + "  <ldap-server url='ldap://springframework.org:389/dc=springframework,dc=org' manager-dn='username' manager-password='password'/>"
            + "  <ldap-authentication-provider user-dn-pattern='uid={0},ou=people'/>"
            + "</authentication-manager>";

    private static final String LDAP_GROUP_XML = "<authentication-manager>"
            + "  <ldap-server url='ldap://springframework.org:389/dc=springframework,dc=org' manager-dn='username' manager-password='password'/>"
            + "  <ldap-authentication-provider user-dn-pattern='uid={0},ou=people' group-search-base='ou=groups'/>"
            + "</authentication-manager>";


    private ConfigurationParserImpl parser = new ConfigurationParserImpl();

    public void testJdbcProviderParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(JDBC_XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        AuthenticationManagerConfiguration configuration = parser.parse(reader);
        AuthenticationProviderConfiguration provider = configuration.getProviderConfigurations().get(0);
        assertTrue(provider instanceof JdbcProviderConfiguration);
        assertEquals("someDataSource", ((JdbcProviderConfiguration) provider).getDataSourceName());
    }

    public void testLdapUserDNParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(LDAP_USERDN_XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        AuthenticationManagerConfiguration configuration = parser.parse(reader);
        LdapServerConfiguration server = configuration.getLdapServerConfiguration();
        assertEquals("ldap://springframework.org:389/dc=springframework,dc=org", server.getServerLocation());
        assertEquals("username", server.getManagerDN());
        assertEquals("password", server.getManagerPassword());
        AuthenticationProviderConfiguration provider = configuration.getProviderConfigurations().get(0);
        assertTrue(provider instanceof LdapProviderConfiguration);
        assertEquals("uid={0},ou=people", ((LdapProviderConfiguration) provider).getDnPattern()[0]);
    }

    public void testLdapGroupSearchBaseParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(LDAP_GROUP_XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        AuthenticationManagerConfiguration configuration = parser.parse(reader);
        LdapServerConfiguration server = configuration.getLdapServerConfiguration();
        assertEquals("ldap://springframework.org:389/dc=springframework,dc=org", server.getServerLocation());
        AuthenticationProviderConfiguration provider = configuration.getProviderConfigurations().get(0);
        assertTrue(provider instanceof LdapProviderConfiguration);
        assertEquals("ou=groups", ((LdapProviderConfiguration) provider).getGroupSearchBase());
    }

}

