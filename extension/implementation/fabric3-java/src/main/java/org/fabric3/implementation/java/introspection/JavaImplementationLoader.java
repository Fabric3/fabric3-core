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
package org.fabric3.implementation.java.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a Java component implementation in a composite.
 */
@EagerInit
@Key(Constants.SCA_PREFIX + "implementation.java")
public class JavaImplementationLoader extends AbstractValidatingTypeLoader<JavaImplementation> {
    private JavaImplementationIntrospector introspector;

    public JavaImplementationLoader(@Reference JavaImplementationIntrospector introspector, @Reference LoaderHelper loaderHelper) {
        this.introspector = introspector;
        addAttributes("class", "requires", "policySets");
    }

    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        JavaImplementation implementation = new JavaImplementation();

        validateAttributes(reader, introspectionContext, implementation);

        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", startLocation);
            introspectionContext.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return implementation;
        }
        Class<?> clazz;
        try {
            clazz = introspectionContext.getClassLoader().loadClass(implClass);
        } catch (ClassNotFoundException e) {
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(implClass, e.getMessage(), implementation);
            introspectionContext.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return null;
        }

        LoaderUtil.skipToEndElement(reader);

        implementation.setImplementationClass(clazz);
        InjectingComponentType componentType = new InjectingComponentType(implClass);
        introspector.introspect(componentType, introspectionContext);
        implementation.setComponentType(componentType);
        return implementation;
    }

}
