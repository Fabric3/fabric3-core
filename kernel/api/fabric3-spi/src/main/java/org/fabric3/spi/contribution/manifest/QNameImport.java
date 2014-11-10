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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * A QName-based contribution import
 */
public class QNameImport implements Import {
    private static final long serialVersionUID = 7714960525252585065L;

    private static final QName QNAME = new QName(Constants.SCA_NS, "import");

    private String namespace;
    private URI location;
    private Map<URI, Export> resolved;

    /**
     * Constructor.
     *
     * @param namespace the imported namespace
     * @param location  the location of the contribution exporting the namespace or null if the contribution should be resolved
     */
    public QNameImport(String namespace, URI location) {
        this.namespace = namespace;
        this.location = location;
        resolved = new HashMap<>();
    }

    public QName getType() {
        return QNAME;
    }

    public String getNamespace() {
        return namespace;
    }

    public URI getLocation() {
        return location;
    }

    public boolean isMultiplicity() {
        return true;
    }

    public boolean isRequired() {
        return true;
    }

    public Map<URI, Export> getResolved() {
        return resolved;
    }

    public void addResolved(URI contributionUri, Export export) {
        resolved.put(contributionUri, export);
    }

    public String toString() {
        return "qname: " + namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QNameImport that = (QNameImport) o;

        return !(location != null ? !location.equals(that.location) : that.location != null)
                && !(namespace != null ? !namespace.equals(that.namespace) : that.namespace != null);

    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }
}
