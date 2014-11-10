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
package org.fabric3.contribution.wire;

import java.net.URI;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * Wires two contributions using the SCA import/export mechanism, making QName-based artifacts exported from one contribution visible to the other
 * importing contribution.
 */
public class QNameContributionWire implements ContributionWire<QNameImport, QNameExport> {
    private static final long serialVersionUID = -2760593628993100399L;
    private QNameImport imprt;
    private QNameExport export;
    private URI importUri;
    private URI exportUri;

    public QNameContributionWire(QNameImport imprt, QNameExport export, URI importUri, URI exportUri) {
        this.imprt = imprt;
        this.export = export;
        this.importUri = importUri;
        this.exportUri = exportUri;
    }

    public QNameImport getImport() {
        return imprt;
    }

    public QNameExport getExport() {
        return export;
    }

    public URI getImportContributionUri() {
        return importUri;
    }

    public URI getExportContributionUri() {
        return exportUri;
    }

    public boolean resolves(Symbol resource) {
        if (!(resource instanceof QNameSymbol)) {
            return false;
        }
        QNameSymbol symbol = (QNameSymbol) resource;
        return imprt.getNamespace().equals(symbol.getKey().getNamespaceURI());
    }
}
