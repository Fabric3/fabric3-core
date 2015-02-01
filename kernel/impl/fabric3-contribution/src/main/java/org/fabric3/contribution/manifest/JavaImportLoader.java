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

import org.fabric3.api.host.Version;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Processes a <code>import.java</code> element in a contribution manifest
 */
@EagerInit
public class JavaImportLoader extends AbstractValidatingTypeLoader<JavaImport> {

    public JavaImportLoader() {
        addAttributes("package", "required", "version", "min", "location", "minInclusive", "max", "maxInclusive");
    }

    public JavaImport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);

        String statement = reader.getAttributeValue(null, "package");
        if (statement == null) {
            MissingPackage failure = new MissingPackage("No package name specified", startLocation);
            context.addError(failure);
            return null;
        }

        String requiredAttr = reader.getAttributeValue(null, "required");
        boolean required = requiredAttr == null || Boolean.parseBoolean(requiredAttr);

        PackageInfo info;
        String version = reader.getAttributeValue(null, "version");
        String minVersion = reader.getAttributeValue(null, "min");
        if (version != null) {
            info = parseVersion(statement, version, required, reader, context);
        } else if (minVersion != null) {
            info = parseRange(statement, minVersion, required, reader, context);
        } else {
            info = new PackageInfo(statement, required);
        }
        if (info == null) {
            // validation error
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

        return new JavaImport(info, locationUri);
    }

    private PackageInfo parseVersion(String statement, String version, boolean required, XMLStreamReader reader, IntrospectionContext context) {
        try {
            String minInclusiveAttr = reader.getAttributeValue(null, "minInclusive");
            boolean minInclusive = minInclusiveAttr == null || Boolean.parseBoolean(minInclusiveAttr);
            Version packageVersion = new Version(version);
            return new PackageInfo(statement, packageVersion, minInclusive, required);
        } catch (IllegalArgumentException e) {
            Location location = reader.getLocation();
            InvalidValue failure = new InvalidValue("Invalid import package version", location, e);
            context.addError(failure);
            return null;
        }
    }

    private PackageInfo parseRange(String statement, String minVersion, boolean required, XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        String minInclusiveAttr = reader.getAttributeValue(null, "minInclusive");
        boolean minInclusive = minInclusiveAttr == null || Boolean.parseBoolean(minInclusiveAttr);
        String maxVersion = reader.getAttributeValue(null, "max");
        Version minimum;
        Version maximum = null;
        try {
            minimum = new Version(minVersion);
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid minimum package version", location, e);
            context.addError(failure);
            return null;
        }
        if (maxVersion != null) {
            try {
                maximum = new Version(maxVersion);
            } catch (IllegalArgumentException e) {
                InvalidValue failure = new InvalidValue("Invalid maximum package version", location, e);
                context.addError(failure);
                return null;
            }
        }
        String maxInclusiveAttr = reader.getAttributeValue(null, "maxInclusive");
        boolean maxInclusive = maxInclusiveAttr == null || Boolean.parseBoolean(maxInclusiveAttr);
        return new PackageInfo(statement, minimum, minInclusive, maximum, maxInclusive, required);
    }

}
