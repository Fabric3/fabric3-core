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
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;

/**
 * Wires two contributions, using the Java import/export mechanism, making a set of classes from the exporting contribution visible to the importing
 * contribution. The semantics of a JavaContributionWire are defined by OSGi R4 bundle imports and exports.
 */
public class JavaContributionWire implements ContributionWire<JavaImport, JavaExport> {
    private static final long serialVersionUID = -2724694051340291455L;
    private JavaImport imprt;
    private JavaExport export;
    private URI importUri;
    private URI exportUri;

    public JavaContributionWire(JavaImport imprt, JavaExport export, URI importUri, URI exportUri) {
        this.imprt = imprt;
        this.export = export;
        this.importUri = importUri;
        this.exportUri = exportUri;
    }

    public JavaImport getImport() {
        return imprt;
    }

    public JavaExport getExport() {
        return export;
    }

    public URI getImportContributionUri() {
        return importUri;
    }

    public URI getExportContributionUri() {
        return exportUri;
    }

    public boolean resolves(Symbol resource) {
        // return false as this wire type is used to resolve classes, which are done via a classloader
        return false;
    }

}
