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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.AuthenticationProviderConfiguration;
import org.fabric3.security.spring.config.ConfigurationParserImpl;
import org.fabric3.security.spring.config.JdbcProviderConfiguration;
import org.fabric3.security.spring.config.LdapProviderConfiguration;
import org.fabric3.security.spring.config.LdapServerConfiguration;

/**
 * @version $Rev$ $Date$
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

