/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.spi.contribution.manifest;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

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
