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

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.host.Version;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;

/**
 * Loads an <code>export.java</code> entry in a contribution manifest.
 */
@EagerInit
public class JavaExportLoader extends AbstractValidatingTypeLoader<JavaExport> {

    public JavaExportLoader() {
        addAttributes("package", "required", "version", "min", "location", "minInclusive", "max", "maxInclusive");
    }

    public JavaExport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);

        String statement = reader.getAttributeValue(null, "package");
        if (statement == null) {
            MissingPackage failure = new MissingPackage("No package name specified", startLocation);
            context.addError(failure);
            return null;
        }
        PackageInfo info;
        String version = reader.getAttributeValue(null, "version");
        if (version != null) {
            Version packageVersion;
            try {
                packageVersion = new Version(version);
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidValue("Invalid export version", startLocation, e));
                packageVersion = new Version("0");
            }
            info = new PackageInfo(statement, packageVersion, true, true);
        } else {
            info = new PackageInfo(statement);
        }
        return new JavaExport(info);
    }


}

