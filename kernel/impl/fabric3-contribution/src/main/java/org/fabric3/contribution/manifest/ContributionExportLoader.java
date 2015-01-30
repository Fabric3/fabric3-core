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

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Processes an <code>export.contribution</code> element in a contribution manifest
 */
@EagerInit
public class ContributionExportLoader extends AbstractValidatingTypeLoader<ContributionExport> {
    private URI INVALID_URI = URI.create("invalid");

    public ContributionExportLoader() {
        addAttributes("uri");
    }

    public ContributionExport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);
        String uriStr = reader.getAttributeValue(null, "uri");
        if (uriStr == null) {
            MissingManifestAttribute failure = new MissingManifestAttribute("The uri attribute must be specified", startLocation);
            context.addError(failure);
            return null;
        }
        try {
            URI uri = new URI(uriStr);
            return new ContributionExport(uri);
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid symbolicUri attribute", startLocation, e);
            context.addError(error);
        }
        return new ContributionExport(INVALID_URI);
    }

}
