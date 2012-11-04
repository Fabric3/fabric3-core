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
package org.fabric3.binding.activemq.factory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.spi.runtime.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.FactoryRegistrationException;
import org.fabric3.host.runtime.HostInfo;

import static org.fabric3.binding.jms.spi.runtime.JmsConstants.DEFAULT_CONNECTION_FACTORY;
import static org.fabric3.binding.jms.spi.runtime.JmsConstants.DEFAULT_XA_CONNECTION_FACTORY;

/**
 *
 */
@EagerInit
public class ConnectionFactoryTemplateRegistryImpl implements ConnectionFactoryTemplateRegistry {
    private ConnectionFactoryConfigurationParser parser;
    private String brokerAddress;
    private URI brokerUri;

    private Map<String, ConnectionFactoryConfiguration> templates = new HashMap<String, ConnectionFactoryConfiguration>();

    public ConnectionFactoryTemplateRegistryImpl(@Reference HostInfo info) {
        parser = new ConnectionFactoryConfigurationParser();
        this.brokerAddress = "vm://" + info.getRuntimeName().replace(":", ".");
        this.brokerUri = URI.create(brokerAddress);
    }

    public ConnectionFactoryConfiguration getTemplate(String name) {
        return templates.get(name);
    }

    @Property(required = false)
    public void setConnectionFactoryTemplates(XMLStreamReader reader) throws XMLStreamException, InvalidConfigurationException {
        List<ConnectionFactoryConfiguration> list = parser.parse(brokerAddress, reader);
        for (ConnectionFactoryConfiguration configuration : list) {
            templates.put(configuration.getName(), configuration);
        }
    }

    @Init
    public void init() throws FactoryRegistrationException {
        // configure default templates if they are not explicitly defined
        if (!templates.containsKey(DEFAULT_CONNECTION_FACTORY)){
            ConnectionFactoryConfiguration template = new ConnectionFactoryConfiguration();
            template.setName(DEFAULT_CONNECTION_FACTORY);
            template.setBrokerUri(brokerUri);
            template.setType(ConnectionFactoryType.LOCAL);
            templates.put(template.getName(), template);
        }
        if (!templates.containsKey(DEFAULT_XA_CONNECTION_FACTORY)){
            ConnectionFactoryConfiguration template = new ConnectionFactoryConfiguration();
            template.setName(DEFAULT_XA_CONNECTION_FACTORY);
            template.setBrokerUri(brokerUri);
            template.setType(ConnectionFactoryType.XA);
            templates.put(template.getName(), template);
        }
    }

}
