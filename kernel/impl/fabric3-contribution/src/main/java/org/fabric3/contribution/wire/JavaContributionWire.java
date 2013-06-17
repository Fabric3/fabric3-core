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
