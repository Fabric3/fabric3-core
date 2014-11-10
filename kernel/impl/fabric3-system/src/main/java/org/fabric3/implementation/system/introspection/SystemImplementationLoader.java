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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.system.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.model.type.system.SystemImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Loads information for a system implementation
 */
@EagerInit
public class SystemImplementationLoader extends AbstractValidatingTypeLoader<SystemImplementation> {
    private ImplementationIntrospector implementationIntrospector;

    /**
     * Constructor.
     *
     * @param implementationIntrospector the component type loader to use
     */
    public SystemImplementationLoader(@Reference ImplementationIntrospector implementationIntrospector) {
        this.implementationIntrospector = implementationIntrospector;
        addAttributes("class");
    }

    public SystemImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        SystemImplementation implementation = new SystemImplementation();

        validateAttributes(reader, introspectionContext, implementation);

        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("Implementation class must be specified using the class attribute", startLocation);
            introspectionContext.addError(failure);
            return null;
        }
        LoaderUtil.skipToEndElement(reader);

        implementation.setImplementationClass(implClass);
        InjectingComponentType componentType = new InjectingComponentType(implClass);
        implementationIntrospector.introspect(componentType, introspectionContext);
        implementation.setComponentType(componentType);
        return implementation;
    }

}
