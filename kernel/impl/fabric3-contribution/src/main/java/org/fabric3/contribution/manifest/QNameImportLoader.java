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
package org.fabric3.contribution.manifest;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Processes a QName-based <code>import</code> element in a contribution manifest
 */
@EagerInit
public class QNameImportLoader extends AbstractValidatingTypeLoader<QNameImport> {

    public QNameImportLoader() {
        addAttributes("namespace", "location");
    }

    public QNameImport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);
        String ns = reader.getAttributeValue(null, "namespace");
        if (ns == null) {
            MissingManifestAttribute failure = new MissingManifestAttribute("The namespace attribute must be specified", startLocation);
            context.addError(failure);
            return null;
        }
        URI locationUri = null;
        String location = reader.getAttributeValue(null, "location");
        if (location != null) {
            try {
                locationUri = new URI(location);
            } catch (URISyntaxException e) {
                InvalidValue error = new InvalidValue("Invalid location attribute", startLocation, e);
                context.addError(error);
            }
        }
        return new QNameImport(ns, locationUri);
    }

}
