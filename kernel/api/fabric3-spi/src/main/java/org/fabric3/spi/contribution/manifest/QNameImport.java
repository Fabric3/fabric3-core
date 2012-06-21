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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * A QName-based contribution import
 *
 * @version $Rev$ $Date$
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
        resolved = new HashMap<URI, Export>();
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
