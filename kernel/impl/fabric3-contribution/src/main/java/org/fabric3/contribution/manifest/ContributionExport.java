/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.contribution.manifest;

import java.net.URI;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * Exports the entire package contents of a contribution. This export type is used for API and SPI contributions where all contents are visible to
 * importing contributions.
 *
 * @version $Rev$ $Date$
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