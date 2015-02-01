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
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Version;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.model.os.OperatingSystemSpec;
import org.oasisopen.sca.annotation.EagerInit;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Processes a <code>library</code> element in a contribution manifest
 */
@EagerInit
public class LibraryLoader extends AbstractValidatingTypeLoader<Library> {
    private static final String OS = "os";
    private static final String LIBRARY = "library";

    public LibraryLoader() {
        addAttributes("name", "processor", "version", "min", "max", "minInclusive", "maxInclusive");
    }

    public Library load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateLibraryAttributes(reader, context);
        String path = reader.getAttributeValue(null, "path");
        if (path == null) {
            MissingPackage failure = new MissingPackage("No path specified for library declaration", startLocation);
            context.addError(failure);
            return null;
        }

        List<OperatingSystemSpec> systems = parseOperatingSystems(reader, context);
        return new Library(path, systems);
    }

    private List<OperatingSystemSpec> parseOperatingSystems(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        List<OperatingSystemSpec> systems = new ArrayList<>();
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (OS.equals(reader.getName().getLocalPart())) {
                        OperatingSystemSpec system = parseOperatingSystem(reader, context);
                        if (system != null) {
                            systems.add(system);
                        }
                        break;
                    }
                    break;
                case END_ELEMENT:
                    if (LIBRARY.equals(reader.getName().getLocalPart())) {
                        return systems;
                    }
                    break;
            }
        }
    }

    /**
     * Parses an operating system entry. Note null may be returned if there was a parsing error.
     *
     * @param reader  the current reader
     * @param context the context to report errors to
     * @return the parsed entry or null
     */
    private OperatingSystemSpec parseOperatingSystem(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingPackage failure = new MissingPackage("No name specified for operating systems declaration", location);
            context.addError(failure);
            return null;
        }

        OperatingSystemSpec system;
        String versionStr = reader.getAttributeValue(null, "version");
        String minVersion = reader.getAttributeValue(null, "min");
        if (versionStr != null) {
            system = parseVersion(name, versionStr, reader, context);
        } else if (minVersion != null) {
            system = parseRange(name, minVersion, reader, context);
        } else {
            String processor = reader.getAttributeValue(null, "processor");
            system = new OperatingSystemSpec(name, processor);
        }
        return system;
    }

    private OperatingSystemSpec parseVersion(String name, String versionStr, XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        try {
            Version version = new Version(versionStr);
            String minInclusiveAttr = reader.getAttributeValue(null, "minInclusive");
            boolean minInclusive = minInclusiveAttr == null || Boolean.parseBoolean(minInclusiveAttr);
            String processor = reader.getAttributeValue(null, "processor");
            return new OperatingSystemSpec(name, processor, version, minInclusive);
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid operating systems version", location, e);
            context.addError(failure);
            return null;
        }
    }

    private OperatingSystemSpec parseRange(String name, String minVersion, XMLStreamReader reader, IntrospectionContext context) {
        String processor = reader.getAttributeValue(null, "processor");

        String minInclusiveAttr = reader.getAttributeValue(null, "minInclusive");
        boolean minInclusive = minInclusiveAttr == null || Boolean.parseBoolean(minInclusiveAttr);
        String maxInclusiveAttr = reader.getAttributeValue(null, "maxInclusive");
        boolean maxInclusive = maxInclusiveAttr == null || Boolean.parseBoolean(maxInclusiveAttr);

        String maxVersion = reader.getAttributeValue(null, "max");
        Version minimum;
        Version maximum = null;
        try {
            minimum = new Version(minVersion);
        } catch (IllegalArgumentException e) {
            Location location = reader.getLocation();
            InvalidValue failure = new InvalidValue("Invalid minimum package version", location, e);
            context.addError(failure);
            return null;
        }
        if (maxVersion != null) {
            try {
                maximum = new Version(maxVersion);
            } catch (IllegalArgumentException e) {
                Location location = reader.getLocation();
                InvalidValue failure = new InvalidValue("Invalid maximum package version", location, e);
                context.addError(failure);
                return null;
            }
        }
        return new OperatingSystemSpec(name, processor, minimum, minInclusive, maximum, maxInclusive);
    }

    private void validateLibraryAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"path".equals(name)) {
                UnrecognizedAttribute failure = new UnrecognizedAttribute(name, location);
                context.addError(failure);
            }
        }
    }

}