/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.componentType;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.oasisopen.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;

/**
 * Loads a generic component type.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ComponentTypeLoader implements TypeLoader<ComponentType> {
    private static final QName COMPONENT_TYPE = new QName(SCA_NS, "componentType");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");

    private final LoaderRegistry registry;
    private final TypeLoader<Property> propertyLoader;

    public ComponentTypeLoader(@Reference LoaderRegistry registry,
                               @Reference(name = "property") TypeLoader<Property> propertyLoader) {
        this.registry = registry;
        this.propertyLoader = propertyLoader;
    }

    @Init
    public void init() {
        registry.registerLoader(COMPONENT_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(COMPONENT_TYPE);
    }

    public QName getXMLType() {
        return COMPONENT_TYPE;
    }

    public ComponentType load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        QName constrainingType = LoaderUtil.getQName(reader.getAttributeValue(null, "constrainingType"),
                                                     introspectionContext.getTargetNamespace(),
                                                     reader.getNamespaceContext());

        ComponentType type = new ComponentType();
        type.setConstrainingType(constrainingType);
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (PROPERTY.equals(qname)) {
                    Property property = propertyLoader.load(reader, introspectionContext);
                    type.add(property);
                } else if (SERVICE.equals(qname)) {
                    ServiceDefinition service = null;
                    try {
                        service = registry.load(reader, ServiceDefinition.class, introspectionContext);
                    } catch (UnrecognizedElementException e) {
                        UnrecognizedElement failure = new UnrecognizedElement(reader);
                        introspectionContext.addError(failure);
                        continue;
                    }
                    type.add(service);
                } else if (REFERENCE.equals(qname)) {
                    ReferenceDefinition reference = null;
                    try {
                        reference = registry.load(reader, ReferenceDefinition.class, introspectionContext);
                    } catch (UnrecognizedElementException e) {
                        UnrecognizedElement failure = new UnrecognizedElement(reader);
                        introspectionContext.addError(failure);
                        continue;
                    }
                    type.add(reference);
                } else {
                    // Extension element - for now try to load and see if we can handle it
                    ModelObject modelObject;
                    try {
                        modelObject = registry.load(reader, ModelObject.class, introspectionContext);
                    } catch (UnrecognizedElementException e) {
                        UnrecognizedElement failure = new UnrecognizedElement(reader);
                        introspectionContext.addError(failure);
                        continue;
                    }
                    if (modelObject instanceof Property) {
                        type.add((Property) modelObject);
                    } else if (modelObject instanceof ServiceDefinition) {
                        type.add((ServiceDefinition) modelObject);
                    } else if (modelObject instanceof ReferenceDefinition) {
                        type.add((ReferenceDefinition) modelObject);
                    } else if (type == null) {
                        // error loading, the element, ignore as an error will have been reported
                        break;
                    } else {
                        introspectionContext.addError(new UnrecognizedElement(reader));
                        continue;
                    }
                }
                break;
            case END_ELEMENT:
                return type;
            }
        }
    }
}
