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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.oasisopen.sca.Constants;

/**
 * Imports a contribution by URI. All contents of the resolved exporting contribution are visible.
 */
public class ContributionImport implements Import {
    private static final long serialVersionUID = 5947082714758125178L;
    private static final QName QNAME = new QName(Constants.SCA_NS, "import.contribution");

    private URI symbolicUri;
    private Map<URI, Export> resolved;

    public ContributionImport(URI symbolicUri) {
        this.symbolicUri = symbolicUri;
        resolved = new HashMap<>();
    }

    public QName getType() {
        return QNAME;
    }

    public URI getLocation() {
        return null;
    }

    public URI getSymbolicUri() {
        return symbolicUri;
    }

    public boolean isMultiplicity() {
        return false;
    }

    public boolean isRequired() {
        return true;
    }

    public Map<URI, Export> getResolved() {
        return resolved;
    }

    public void addResolved(URI contributionUri, Export export) {
        if (!resolved.isEmpty()) {
            throw new IllegalArgumentException("Import can resolve to only one export");
        }
        resolved.put(contributionUri, export);
    }

    public String toString() {
        return "contribution [" + symbolicUri + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContributionImport that = (ContributionImport) o;

        return !(symbolicUri != null ? !symbolicUri.equals(that.symbolicUri) : that.symbolicUri != null);

    }

    @Override
    public int hashCode() {
        return symbolicUri != null ? symbolicUri.hashCode() : 0;
    }
}
