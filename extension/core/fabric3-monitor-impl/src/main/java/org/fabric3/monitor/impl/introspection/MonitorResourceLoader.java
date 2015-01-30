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
package org.fabric3.monitor.impl.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.monitor.impl.model.type.DefaultMonitorDestinationDefinition;
import org.fabric3.monitor.spi.model.type.AppenderDefinition;
import org.fabric3.monitor.spi.model.type.MonitorResource;
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
public class MonitorResourceLoader extends AbstractValidatingTypeLoader<MonitorResource> {
    private static final QName SCA_TYPE = new QName(Constants.SCA_NS, "monitor");
    private static final QName F3_TYPE = new QName(org.fabric3.api.Namespaces.F3, "monitor");

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

    public MonitorResource load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MonitorResource definition = new MonitorResource("");
            MissingAttribute error = new MissingAttribute("A monitor name must be specified", reader.getLocation(), definition);
            context.addError(error);
            return definition;
        }

        MonitorResource definition = new MonitorResource(name);
        DefaultMonitorDestinationDefinition destinationDefinition = new DefaultMonitorDestinationDefinition();
        definition.setDestinationDefinition(destinationDefinition);

        Set<String> definedTypes = new HashSet<>();
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
