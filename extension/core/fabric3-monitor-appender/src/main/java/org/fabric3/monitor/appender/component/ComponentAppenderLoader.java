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
package org.fabric3.monitor.appender.component;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a {@link ComponentAppenderDefinition} from an appender configuration.
 */
@EagerInit
public class ComponentAppenderLoader extends AbstractValidatingTypeLoader<ComponentAppenderDefinition> {
    private static final QName SCA_TYPE = new QName(Constants.SCA_NS, "appender.component");
    private static final QName F3_TYPE = new QName(org.fabric3.api.Namespaces.F3, "appender.component");

    private LoaderRegistry registry;

    public ComponentAppenderLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
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

    public ComponentAppenderDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        addAttributes("name");
        validateAttributes(reader, context);
        String componentName = reader.getAttributeValue(null, "name");
        if (componentName == null) {
            ComponentAppenderDefinition definition = new ComponentAppenderDefinition("");
            MissingAttribute error = new MissingAttribute("A component name must be defined for the appender", reader.getLocation(), definition);
            context.addError(error);
            return definition;
        }
        return new ComponentAppenderDefinition(componentName);
    }
}
