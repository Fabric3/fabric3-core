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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.jpa.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.jpa.override.DuplicateOverridesException;
import org.fabric3.jpa.override.OverrideRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a <code>&lt;persistenceUnit&gt;</code> element in a composite.
 */
@EagerInit
public class PersistenceUnitOverridesElementLoader implements TypeLoader<ModelObject> {
    private static final QName QNAME = new QName(org.fabric3.api.Namespaces.F3, "persistenceUnit");

    private LoaderRegistry loaderRegistry;
    private OverrideRegistry overrideRegistry;

    public PersistenceUnitOverridesElementLoader(@Reference LoaderRegistry loaderRegistry, @Reference OverrideRegistry overrideRegistry) {
        this.loaderRegistry = loaderRegistry;
        this.overrideRegistry = overrideRegistry;
    }

    @Init
    public void init() {
        loaderRegistry.registerLoader(QNAME, this);
    }

    @Destroy
    public void destroy() {
        loaderRegistry.unregisterLoader(QNAME);
    }

    public ModelObject load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Persistence unit name not specified", startLocation);
            context.addError(error);
            LoaderUtil.skipToEndElement(reader);
            return null;
        }
        Map<String, String> properties = new HashMap<>();
        while (true) {
            int val = reader.nextTag();
            switch (val) {
            case XMLStreamConstants.START_ELEMENT:
                if ("property".equals(reader.getName().getLocalPart())) {
                    String propertyName = reader.getAttributeValue(null, "name");
                    String propertyValue = reader.getAttributeValue(null, "value");
                    properties.put(propertyName, propertyValue);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("persistenceUnit".equals(reader.getName().getLocalPart())) {
                    try {
                        URI uri = context.getContributionUri();
                        PersistenceOverrides overrides = new PersistenceOverrides(name, properties);
                        overrideRegistry.register(uri, overrides);
                    } catch (DuplicateOverridesException e) {
                        DuplicateOverrides error = new DuplicateOverrides(name, startLocation);
                        context.addError(error);
                    }
                    return null;
                }
                break;
            }
        }
    }


}
