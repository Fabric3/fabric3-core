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
package org.fabric3.spi.contribution;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.stream.Source;

/**
 * The base representation of a deployed contribution
 */
public class Contribution implements Serializable {
    private static final long serialVersionUID = 2511879480122631196L;

    private URI uri;
    private transient Source source;
    private ContributionState state = ContributionState.STORED;
    private URL location;
    private long timestamp;
    private String contentType;
    private ContributionManifest manifest = new ContributionManifest();
    private transient List<Resource> resources = new ArrayList<>();
    private transient Map<Object, Object> metadata = new HashMap<>();
    private List<ContributionWire<?, ?>> wires = new ArrayList<>();
    private List<URI> resolvedExtensionProviders = new ArrayList<>();

    private List<QName> lockOwners = new ArrayList<>();

    private List<URL> additionalLocations = new ArrayList<>();

    public Contribution(URI uri) {
        this.uri = uri;
    }

    /**
     * Constructor.
     *  @param uri         the contribution URI
     * @param source      the source for reading the contribution contents
     * @param location    the URL for the contribution archive. This can be null for contributions that are not physical archives.
     * @param timestamp   the contribution artifact time stamp
     * @param contentType the contribution MIME type
     */
    public Contribution(URI uri, Source source, URL location, long timestamp, String contentType) {
        this.uri = uri;
        this.source = source;
        this.location = location;
        this.timestamp = timestamp;
        this.contentType = contentType;
    }

    /**
     * Returns the contribution URI.
     *
     * @return the contribution URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the contribution lifecycle state.
     *
     * @return the contribution lifecycle state
     */
    public ContributionState getState() {
        return state;
    }

    /**
     * Sets the contribution lifecycle state.
     *
     * @param state the contribution lifecycle state
     */
    public void setState(ContributionState state) {
        this.state = state;
    }

    /**
     * Returns the local URL for the contribution artifact or null if the contribution is not a physical artifact (e.g. it is synthesized from some source).
     *
     * @return the dereferenceable URL for the contribution artifact or null
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Used to override the contribution location during processing.
     *
     * @param location the new contribution location
     */
    public void setLocation(URL location) {
        this.location = location;
    }

    /**
     * Returns additional content locations for the contribution. For example, exploded web applications may be composed of multiple directories mapped from a
     * development environment.
     *
     * @return the locations.
     */
    public List<URL> getAdditionalLocations() {
        return additionalLocations;
    }

    /**
     * Adds an additional content location.
     *
     * @param location the location
     */
    public void addAdditionalLocation(URL location) {
        additionalLocations.add(location);
    }

    /**
     * Returns the MIME type for the contribution.
     *
     * @return the MIME type for the contribution
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns a source for reading contents of the contribution.
     *
     * @return a source for reading contents of the contribution
     */
    public Source getSource() {
        return source;
    }

    /**
     * Returns the timestamp of the most recent update to the artifact.
     *
     * @return the timestamp of the most recent update to the artifact
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the contribution manifest.
     *
     * @return the contribution manifest
     */
    public ContributionManifest getManifest() {
        return manifest;
    }

    /**
     * Sets the contribution manifest.
     *
     * @param manifest the contribution manifest
     */
    public void setManifest(ContributionManifest manifest) {
        this.manifest = manifest;
    }

    /**
     * Adds a resource to the contribution.
     *
     * @param resource the resource
     */
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     * Returns the list of resources for the contribution.
     *
     * @return the list of resources
     */
    public List<Resource> getResources() {
        return resources;
    }

    /**
     * Adds a wire for an import
     *
     * @param wire the wire
     */
    public void addWire(ContributionWire<?, ?> wire) {
        wires.add(wire);
    }

    /**
     * Returns the wires for this contribution.
     *
     * @return the wires for this contribution
     */
    public List<ContributionWire<?, ?>> getWires() {
        return wires;
    }

    /**
     * Adds the URI of the resolved extension provider.
     *
     * @param uri the URI of the resolved extension provider.
     */
    public void addResolvedExtensionProvider(URI uri) {
        resolvedExtensionProviders.add(uri);
    }

    /**
     * Gets the URIs of the resolved extension providers.
     *
     * @return the URIs of the resolved extension providers
     */
    public List<URI> getResolvedExtensionProviders() {
        return resolvedExtensionProviders;
    }

    /**
     * Adds metadata to the context.
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    public void addMetaData(Object key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Removes metadata to the context.
     *
     * @param key the metadata key
     */
    public void removeMetaData(Object key) {
        metadata.remove(key);
    }

    /**
     * Returns metadata stored metadata.
     *
     * @param type the expected metadata type
     * @param key  the metadata key
     * @return the metadata value or null if not found
     */
    public <T> T getMetaData(Class<T> type, Object key) {
        return type.cast(metadata.get(key));
    }

    /**
     * Acquires a lock for the contribution. If a contribution is locked, it cannot be uninstalled. Locks may be acquired by multiple owners, for example,
     * deployable composites that are contained in a contribution when they are deployed.
     *
     * @param owner the lock owner
     */
    public void acquireLock(QName owner) {
        if (lockOwners.contains(owner)) {
            throw new IllegalStateException("Lock already held by owner for contribution " + uri + " :" + owner);
        }
        lockOwners.add(owner);
    }

    /**
     * Releases a lock held by the given owner.
     *
     * @param owner the lock owner
     */
    public void releaseLock(QName owner) {
        if (lockOwners.isEmpty()) {
            return;
        }
        if (!lockOwners.remove(owner)) {
            throw new IllegalStateException("Lock not held by owner for contribution " + uri + " :" + owner);
        }
    }

    /**
     * Returns the set of current lock owners ordered by time of lock acquisition.
     *
     * @return the set of current lock owners
     */
    public List<QName> getLockOwners() {
        return lockOwners;
    }

    /**
     * Returns true if the contribution is locked. Locked contributions cannot be uninstalled.
     *
     * @return true if the contribution is locked
     */
    public boolean isLocked() {
        return !lockOwners.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contribution that = (Contribution) o;
        return !(uri != null ? !uri.equals(that.uri) : that.uri != null);
    }

}
