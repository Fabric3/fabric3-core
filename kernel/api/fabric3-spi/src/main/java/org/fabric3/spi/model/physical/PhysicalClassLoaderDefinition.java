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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.spi.model.os.Library;

/**
 * Used to provision classloaders on a runtime. Defined classloaders correspond to a contribution.
 */
public class PhysicalClassLoaderDefinition implements Serializable {
    private static final long serialVersionUID = 1869864181383360066L;

    private URI uri;
    private boolean provisionArtifact;
    private Set<PhysicalClassLoaderWireDefinition> wireDefinitions = new LinkedHashSet<PhysicalClassLoaderWireDefinition>();
    private List<Library> libraries = Collections.emptyList();

    /**
     * Constructor.
     *
     * @param uri               the URI of the contribution associated with the classloader
     * @param provisionArtifact true if the associated contribution should be provisioned. Synthetic contributions do not need to be provisioned.
     */
    public PhysicalClassLoaderDefinition(URI uri, boolean provisionArtifact) {
        this.uri = uri;
        this.provisionArtifact = provisionArtifact;
    }

    /**
     * Constructor.
     *
     * @param uri               the URI of the contribution associated with the classloader
     * @param libraries         metadata for native libraries bundled in the contribution
     * @param provisionArtifact true if the associated contribution should be provisioned. Synthetic contributions do not need to be provisioned.
     */
    public PhysicalClassLoaderDefinition(URI uri, List<Library> libraries, boolean provisionArtifact) {
        this.uri = uri;
        this.libraries = libraries;
        this.provisionArtifact = provisionArtifact;
    }

    /**
     * Returns the classloader uri.
     *
     * @return the classloader uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * True if the artifact should be provisioned.
     *
     * @return true if the artifact should be provisioned
     */
    public boolean isProvisionArtifact() {
        return provisionArtifact;
    }

    /**
     * Adds a PhysicalClassLoaderWireDefinition that describes a wire to another contribution classloader.
     *
     * @param definition the PhysicalClassLoaderDefinition
     */
    public void add(PhysicalClassLoaderWireDefinition definition) {
        wireDefinitions.add(definition);
    }

    /**
     * Returns a set of PhysicalClassLoaderWireDefinition that describe the wires to other contribution classloaders.
     *
     * @return a set of PhysicalClassLoaderWireDefinition that describe the wires to other contribution classloader
     */
    public Set<PhysicalClassLoaderWireDefinition> getWireDefinitions() {
        return wireDefinitions;
    }

    /**
     * Returns metadata for native libraries bundled in the contribution.
     *
     * @return metadata for native libraries bundled in the contribution
     */
    public List<Library> getLibraries() {
        return libraries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhysicalClassLoaderDefinition that = (PhysicalClassLoaderDefinition) o;

        return !(uri != null ? !uri.equals(that.uri) : that.uri != null)
                && !(wireDefinitions != null ? !wireDefinitions.equals(that.wireDefinitions) : that.wireDefinitions != null);

    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (wireDefinitions != null ? wireDefinitions.hashCode() : 0);
        return result;
    }
}
