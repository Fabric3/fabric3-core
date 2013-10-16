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
 *
 */
package org.fabric3.jpa.introspection;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.jpa.override.DuplicateOverridesException;
import org.fabric3.jpa.override.OverrideRegistry;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;

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
        Map<String, String> properties = new HashMap<String, String>();
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
