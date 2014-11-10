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
    private Set<PhysicalClassLoaderWireDefinition> wireDefinitions = new LinkedHashSet<>();
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
