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

import java.net.URI;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * Exports the entire package contents of a contribution. This export type is used for API and SPI contributions where all contents are visible to
 * importing contributions.
 */
public class ContributionExport implements Export {
    private static final long serialVersionUID = -2400233923134603994L;

    private static final QName QNAME = new QName(Constants.SCA_NS, "export.contribution");

    private URI symbolicUri;
    private boolean resolved;

    public ContributionExport(URI symbolicUri) {
        this.symbolicUri = symbolicUri;
    }

    public QName getType() {
        return QNAME;
    }

    public URI getSymbolicUri() {
        return symbolicUri;
    }

    public URI getLocation() {
        return null;
    }

    public boolean match(Import imprt) {
        return imprt instanceof ContributionImport && symbolicUri.equals(((ContributionImport) imprt).getSymbolicUri());
    }

    public boolean isResolved() {
        return resolved;
    }

    public void resolve() {
        resolved = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContributionExport that = (ContributionExport) o;

        return !(symbolicUri != null ? !symbolicUri.equals(that.symbolicUri) : that.symbolicUri != null);

    }

    @Override
    public int hashCode() {
        return symbolicUri != null ? symbolicUri.hashCode() : 0;
    }
}