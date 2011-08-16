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
package org.fabric3.spi.contribution.manifest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * Represents an <code>import.java</code> entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
public class JavaImport implements Import {
    private static final long serialVersionUID = -7863768515125756048L;
    private URI location;
    private PackageInfo packageInfo;
    private Map<URI, Export> resolved;

    public JavaImport(PackageInfo packageInfo) {
        this(packageInfo, null);
    }

    public JavaImport(PackageInfo packageInfo, URI location) {
        if (packageInfo == null) {
            throw new IllegalArgumentException("Package info cannot be null");
        }
        this.packageInfo = packageInfo;
        this.location = location;
        resolved = new HashMap<URI, Export>();
    }

    public URI getLocation() {
        return location;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public boolean isMultiplicity() {
        return false;
    }

    public boolean isRequired() {
        return packageInfo.isRequired();
    }

    public Map<URI, Export> getResolved() {
        return resolved;
    }

    public void addResolved(URI contributionUri, Export export) {
        if (!resolved.isEmpty()) {
            URI entry = resolved.keySet().iterator().next();
            String s = "Import [" + packageInfo + "] must resolve to only one export. Multiple exporting contributions found: " + entry + " and "
                    + contributionUri;
            throw new IllegalArgumentException(s);
        }
        resolved.put(contributionUri, export);
    }

    public String toString() {
        return "[" + packageInfo + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaImport that = (JavaImport) o;

        return !(location != null ? !location.equals(that.location) : that.location != null)
                && !(packageInfo != null ? !packageInfo.equals(that.packageInfo) : that.packageInfo != null);

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (packageInfo != null ? packageInfo.hashCode() : 0);
        return result;
    }
}
