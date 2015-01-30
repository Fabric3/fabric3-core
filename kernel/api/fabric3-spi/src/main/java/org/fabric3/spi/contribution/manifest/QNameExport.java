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

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.oasisopen.sca.Constants;

/**
 * A QName-based contribution export
 */
public class QNameExport implements Export {
    private static final long serialVersionUID = -6813997109078522174L;

    private static final QName QNAME = new QName(Constants.SCA_NS, "export");

    private String namespace;
    private boolean resolved;

    public QNameExport(String namespace) {
        this.namespace = namespace;
    }

    public QName getType() {
        return QNAME;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean match(Import contributionImport) {
        return contributionImport instanceof QNameImport && ((QNameImport) contributionImport).getNamespace().equals(namespace);
    }

    public boolean isResolved() {
        return resolved;
    }

    public void resolve() {
        resolved = true;
    }

    public String toString() {
        return "qname: " + namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QNameExport that = (QNameExport) o;

        return !(namespace != null ? !namespace.equals(that.namespace) : that.namespace != null);

    }

    @Override
    public int hashCode() {
        return namespace != null ? namespace.hashCode() : 0;
    }
}
