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
package org.fabric3.spi.introspection.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.ModelObject;

/**
 * Failure that indicates an element was encountered that could not be handled.
 */
public class UnrecognizedElement extends XmlValidationFailure {
    private QName name;

    public UnrecognizedElement(XMLStreamReader reader, Location location, ModelObject... sources) {
        super("Unrecognized element", location, sources);
        name = reader.getName();
    }

    public String getMessage() {
        String namespace = name.getNamespaceURI();
        if (DeprecatedNamespaceHelper.isDeprecatedNamespace(namespace)) {
            if (getLine() == -1) {
                return "The element specified in " + getResourceURI() + " uses the deprecated namespace "
                        + namespace + ". Please change it to " + org.fabric3.api.Namespaces.F3;
            }
            return "The element specified in " + getResourceURI() + " at " + getLine() + "," + getColumn() + " uses the deprecated namespace "
                    + namespace + ". Please change it to " + org.fabric3.api.Namespaces.F3;
        } else {
            if (getLine() == -1) {
                return "The element " + name + " specified in " + getResourceURI() + " was not recognized. "
                        + "If this is not a typo, check to ensure extensions are configured properly.";
            }
            return "The element " + name + " specified in " + getResourceURI() + " at " + getLine() + "," + getColumn() + " was not recognized. "
                    + "If this is not a typo, check to ensure extensions are configured properly.";
        }
    }

    public String getShortMessage() {
        String namespace = name.getNamespaceURI();
        if (DeprecatedNamespaceHelper.isDeprecatedNamespace(namespace)) {
            return "The element uses the deprecated namespace " + namespace + ". Please change it to " + org.fabric3.api.Namespaces.F3;
        } else {
            return "The element " + name + " was not recognized";
        }
    }

}