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
*/
package org.fabric3.contribution.manifest;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.host.Version;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.model.os.OperatingSystemSpec;

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
        List<OperatingSystemSpec> systems = new ArrayList<OperatingSystemSpec>();
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