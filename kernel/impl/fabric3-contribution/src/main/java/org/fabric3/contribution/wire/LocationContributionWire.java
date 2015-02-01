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

import org.fabric3.contribution.manifest.ContributionExport;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Symbol;

/**
 * Wires two contributions using an explicit target contribution reference, making all artifacts exported from one contribution visible to the other importing
 * contribution.
 */
public class LocationContributionWire implements ContributionWire<ContributionImport, ContributionExport> {
    private static final long serialVersionUID = -2724694051340291455L;
    private ContributionImport imprt;
    private ContributionExport export;
    private URI importUri;
    private URI exportUri;

    public LocationContributionWire(ContributionImport imprt, ContributionExport export, URI importUri, URI exportUri) {
        this.imprt = imprt;
        this.export = export;
        this.importUri = importUri;
        this.exportUri = exportUri;
    }

    public ContributionImport getImport() {
        return imprt;
    }

    public ContributionExport getExport() {
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