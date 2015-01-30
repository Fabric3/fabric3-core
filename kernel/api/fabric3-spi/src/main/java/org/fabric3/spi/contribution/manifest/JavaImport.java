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
package org.fabric3.spi.contribution.manifest;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.oasisopen.sca.Constants;

/**
 * Represents an <code>import.java</code> entry in a contribution manifest.
 */
public class JavaImport implements Import {
    private static final long serialVersionUID = -7863768515125756048L;

    private static final QName QNAME = new QName(Constants.SCA_NS, "import.java");

    private URI location;
    private PackageInfo packageInfo;
    private Map<URI, Export> resolved;

    public JavaImport(PackageInfo packageInfo) {
        this(packageInfo, null);
    }

    public JavaImport(PackageInfo packageInfo, URI location) {
        if (packageInfo == null) {
            throw new IllegalArgumentException("Package info cannot be null");
        }
        this.packageInfo = packageInfo;
        this.location = location;
        resolved = new HashMap<>();
    }

    public QName getType() {
        return QNAME;
    }

    public URI getLocation() {
        return location;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public boolean isMultiplicity() {
        return false;
    }

    public boolean isRequired() {
        return packageInfo.isRequired();
    }

    public Map<URI, Export> getResolved() {
        return resolved;
    }

    public void addResolved(URI contributionUri, Export export) {
        if (!resolved.isEmpty()) {
            URI entry = resolved.keySet().iterator().next();
            String s = "Import [" + packageInfo + "] must resolve to only one export. Multiple exporting contributions found: " + entry + " and "
                    + contributionUri;
            throw new IllegalArgumentException(s);
        }
        resolved.put(contributionUri, export);
    }

    public String toString() {
        return packageInfo.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaImport that = (JavaImport) o;

        return !(location != null ? !location.equals(that.location) : that.location != null)
                && !(packageInfo != null ? !packageInfo.equals(that.packageInfo) : that.packageInfo != null);

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (packageInfo != null ? packageInfo.hashCode() : 0);
        return result;
    }
}
