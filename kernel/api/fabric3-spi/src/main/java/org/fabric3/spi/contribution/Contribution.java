/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * The base representation of a deployed contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Contribution implements Serializable {
    private static final long serialVersionUID = 2511879480122631196L;

    private final URI uri;
    private ContributionState state = ContributionState.STORED;
    private List<URI> profiles;
    private URL location;
    private byte[] checksum;
    private long timestamp;
    private String contentType;
    private boolean persistent;
    private ContributionManifest manifest = new ContributionManifest();
    private List<Resource> resources = new ArrayList<Resource>();
    private List<ContributionWire<?, ?>> wires = new ArrayList<ContributionWire<?, ?>>();
    private List<URI> resolvedExtensionProviders = new ArrayList<URI>();

    private Set<QName> lockOwners = new HashSet<QName>();

    public Contribution(URI uri) {
        this.uri = uri;
        profiles = new ArrayList<URI>();
    }

    /**
     * Instantiates a new Contribution instance.
     *
     * @param uri         the contribution URI
     * @param profiles    the profiles this contribution is a member of
     * @param location    a dereferenceble URL for the contribution archive
     * @param checksum    the checksum for the contribution artifact
     * @param timestamp   the time stamp of the contribution artifact
     * @param contentType the MIME type of the contribution
     * @param persistent  true if the contribution is persistent
     */
    public Contribution(URI uri, List<URI> profiles, URL location, byte[] checksum, long timestamp, String contentType, boolean persistent) {
        this.uri = uri;
        this.profiles = profiles;
        this.location = location;
        this.checksum = checksum;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.persistent = persistent;
    }

    /**
     * Instantiates a new Contribution instance.
     *
     * @param uri         the contribution URI
     * @param location    a dereferenceble URL for the contribution archive
     * @param checksum    the checksum for the contribution artifact
     * @param timestamp   the time stamp of the contribution artifact
     * @param contentType the MIME type of the contribution
     * @param persistent  true if the contribution is persistent
     */
    public Contribution(URI uri, URL location, byte[] checksum, long timestamp, String contentType, boolean persistent) {
        this.uri = uri;
        this.profiles = new ArrayList<URI>();
        this.location = location;
        this.checksum = checksum;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.persistent = persistent;
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

    public List<URI> getProfiles() {
        return profiles;
    }

    public void addProfile(URI uri) {
        profiles.add(uri);
    }

    public void addProfiles(List<URI> uris) {
        profiles.addAll(uris);
    }

    /**
     * Returns the locally dereferenceable URL for the contribution artifact.
     *
     * @return the dereferenceable URL for the contribution artifact
     */
    public URL getLocation() {
        return location;
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
     * Returns the contribution artifact checksum.
     *
     * @return the contribution artifact checksum
     */
    public byte[] getChecksum() {
        return checksum;
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
     * Returns true if the contribution is persistent.
     *
     * @return true if the contribution is persistent.
     */
    public boolean isPersistent() {
        return persistent;
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
     * Returns a ResourceElement matching the symbol or null if not found.
     *
     * @param symbol the symbol to match
     * @return a ResourceElement matching the symbol or null if not found
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Symbol> ResourceElement<T, Serializable> findResourceElement(Symbol<T> symbol) {
        for (Resource resource : resources) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol().equals(symbol)) {
                    return (ResourceElement<T, Serializable>) element;
                }
            }
        }
        return null;
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
     * Acquires a lock for the contribution. If a contribution is locked, it cannot be uninstalled. Locks may be acquired by multiple owners, for
     * example, deployable composites that are contained in a contribution when they are deployed.
     *
     * @param owner the lock owner
     */
    public void acquireLock(QName owner) {
        if (lockOwners.contains(owner)) {
            throw new IllegalStateException("Lock already held by owner for contribution" + uri + " :" + owner);
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
     * Returns the set of current lock owners.
     *
     * @return the set of current lock owners
     */
    public Set<QName> getLockOwners() {
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

    /**
     * Removes the profile from the contribution. Contributions track the profiles they are members of.
     *
     * @param uri the profile URI
     */
    public void removeProfile(URI uri) {
        profiles.remove(uri);
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
