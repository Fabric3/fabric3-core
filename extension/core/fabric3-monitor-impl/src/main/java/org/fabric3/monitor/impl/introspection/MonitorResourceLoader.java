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
package org.fabric3.monitor.impl.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.ModelObject;
import org.fabric3.monitor.impl.model.type.DefaultMonitorDestinationDefinition;
import org.fabric3.monitor.spi.model.type.AppenderDefinition;
import org.fabric3.monitor.spi.model.type.MonitorResourceDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a monitor resource configuration.
 */
@EagerInit
public class MonitorResourceLoader extends AbstractValidatingTypeLoader<MonitorResourceDefinition> {
    private static final QName SCA_TYPE = new QName(Constants.SCA_NS, "monitor");
    private static final QName F3_TYPE = new QName(Namespaces.F3, "monitor");

    private LoaderRegistry registry;

    public MonitorResourceLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
        addAttributes("name");
    }

    @Init
    public void init() {
        // register under both namespaces
        registry.registerLoader(F3_TYPE, this);
        registry.registerLoader(SCA_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(F3_TYPE);
        registry.unregisterLoader(SCA_TYPE);
    }

    public MonitorResourceDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MonitorResourceDefinition definition = new MonitorResourceDefinition("");
            MissingAttribute error = new MissingAttribute("A monitor name must be specified", reader.getLocation(), definition);
            context.addError(error);
            return definition;
        }

        MonitorResourceDefinition definition = new MonitorResourceDefinition(name);
        DefaultMonitorDestinationDefinition destinationDefinition = new DefaultMonitorDestinationDefinition();
        definition.setDestinationDefinition(destinationDefinition);

        Set<String> definedTypes = new HashSet<String>();
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (reader.getName().getLocalPart().startsWith("appender.")) {

                        ModelObject modelObject = registry.load(reader, ModelObject.class, context);
                        if (modelObject instanceof AppenderDefinition) {
                            AppenderDefinition appenderDefinition = (AppenderDefinition) modelObject;
                            String type = appenderDefinition.getType();
                            if (definedTypes.contains(type)) {
                                Location location = reader.getLocation();
                                context.addError(new InvalidValue("Multiple appenders defined with type:" + type, location, definition));
                                continue;
                            }
                            definedTypes.add(type);
                            destinationDefinition.add(appenderDefinition);

                        } else {
                            throw new AssertionError("Unexpected type: " + modelObject);
                        }

                        break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("monitor".equals(reader.getName().getLocalPart())) {
                        return definition;
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    throw new AssertionError("End of document encountered");

            }

        }
    }

}
