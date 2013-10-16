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

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.host.Version;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;

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

    private PackageInfo parseVersion(String statement,
                                     String version,
                                     boolean required,
                                     XMLStreamReader reader,
                                     IntrospectionContext context) {
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

    private PackageInfo parseRange(String statement,
                                   String minVersion,
                                   boolean required,
                                   XMLStreamReader reader,
                                   IntrospectionContext context) {
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
