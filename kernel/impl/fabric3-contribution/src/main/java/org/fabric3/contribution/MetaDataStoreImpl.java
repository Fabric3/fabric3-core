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
package org.fabric3.contribution;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistry;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.contribution.UnresolvedImportException;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default MetaDataStore implementation.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImpl implements MetaDataStore {
    private Map<URI, Contribution> cache = new ConcurrentHashMap<URI, Contribution>();
    private Map<QName, Map<Export, List<Contribution>>> exportsToContributionCache = new ConcurrentHashMap<QName, Map<Export, List<Contribution>>>();
    private ProcessorRegistry processorRegistry;
    private ContributionWireInstantiatorRegistry instantiatorRegistry;

    public MetaDataStoreImpl(ProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    /**
     * Used to reinject the processor registry after runtime bootstrap.
     *
     * @param processorRegistry the configured processor registry
     */
    @Reference
    public void setProcessorRegistry(ProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Reference
    public void setInstantiatorRegistry(ContributionWireInstantiatorRegistry instantiatorRegistry) {
        this.instantiatorRegistry = instantiatorRegistry;
    }

    public void store(Contribution contribution) throws StoreException {
        cache.put(contribution.getUri(), contribution);
        addToExports(contribution);
    }

    public Contribution find(URI contributionUri) {
        return cache.get(contributionUri);
    }

    public Set<Contribution> getContributions() {
        return new HashSet<Contribution>(cache.values());
    }

    public void remove(URI contributionUri) {
        Contribution contribution = find(contributionUri);
        if (contribution != null) {
            List<Export> exports = contribution.getManifest().getExports();
            for (Export export : exports) {
                Map<Export, List<Contribution>> types = exportsToContributionCache.get(export.getType());
                if (types == null) {
                    // programming error
                    throw new AssertionError("Export type not found: " + export.getType());
                }
                for (Iterator<Map.Entry<Export, List<Contribution>>> it = types.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Export, List<Contribution>> contributions = it.next();
                    contributions.getValue().remove(contribution);
                    if (contributions.getValue().isEmpty()) {
                        // if there are no exporting contributions left, remove it
                        it.remove();
                    }
                }
            }
        }
        cache.remove(contributionUri);
    }

    @SuppressWarnings({"unchecked"})
    public <S extends Symbol, V extends Serializable> ResourceElement<S, V> find(Class<V> type, S symbol) {
        for (Contribution contribution : cache.values()) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        if (ResourceState.PROCESSED != resource.getState()) {
                            // this is a programming error as resolve(Symbol) should only be called after contribution resources have been processed
                            throw new AssertionError("Attempt to resolve a resource before it is processed or is in error");
                        }
                        return (ResourceElement<S, V>) element;
                    }
                }
            }
        }
        return null;
    }

    public <S extends Symbol, V extends Serializable> ResourceElement<S, V> find(URI uri, Class<V> type, S symbol) throws StoreException {
        return resolve(uri, type, symbol, null);
    }

    public <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(URI uri, Class<V> type, S symbol, IntrospectionContext context)
            throws StoreException {
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new ContributionResolutionException("Contribution not found: " + identifier, identifier);
        }

        return resolve(contribution, type, symbol, context);
    }

    private <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(Contribution contribution,
                                                                                     Class<V> type,
                                                                                     S symbol,
                                                                                     IntrospectionContext context) throws StoreException {
        ResourceElement<S, V> element;
        // resolve by delegating to exporting contributions first
        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            if (!wire.resolves(symbol)) {
                // the wire doesn't resolve the specific resource
                continue;
            }
            URI resolvedUri = wire.getExportContributionUri();
            Contribution resolved = cache.get(resolvedUri);
            if (resolved == null) {
                // programming error
                throw new AssertionError("Dependent contribution not found: " + resolvedUri);
            }
            element = resolve(resolved, type, symbol, context);
            if (element != null) {
                return element;
            }
        }
        element = resolveInternal(contribution, type, symbol, context);
        if (element != null) {
            return element;
        }

        return null;
    }

    public List<Contribution> resolve(URI uri, Import imprt) {
        Map<Export, List<Contribution>> exports = exportsToContributionCache.get(imprt.getType());
        URI location = imprt.getLocation();
        List<Contribution> resolved = new ArrayList<Contribution>();
        if (exports != null) {
            for (Map.Entry<Export, List<Contribution>> entry : exports.entrySet()) {
                Export export = entry.getKey();
                // also compare the contribution URI to avoid resolving to a contribution that imports and exports the same namespace
                if (Export.EXACT_MATCH == export.match(imprt)) {
                    for (Contribution contribution : entry.getValue()) {
                        if (location != null) {
                            // location is specified, resolve to the explicit contribution with that export
                            if (location.equals(contribution.getUri())) {
                                resolved.add(contribution);
                                return resolved;   // finished, since location is used to specify exactly one contribution
                            }
                        } else {
                            if (!uri.equals(contribution.getUri())) {
                                resolved.add(contribution);
                            }
                        }
                    }

                }
            }
        }
        return resolved;
    }

    public List<ContributionWire<?, ?>> resolveContributionWires(URI uri, Import imprt) throws UnresolvedImportException {
        Map<Export, List<Contribution>> map = exportsToContributionCache.get(imprt.getType());
        if (map == null) {
            return null;
        }
        List<ContributionWire<?, ?>> wires = new ArrayList<ContributionWire<?, ?>>();
        for (Map.Entry<Export, List<Contribution>> entry : map.entrySet()) {
            Export export = entry.getKey();
            int level = export.match(imprt);
            if (level == Export.EXACT_MATCH) {
                if (instantiatorRegistry == null) {
                    // Programming error: an illegal attempt to resolve a contribution before bootstrap has completed.
                    throw new AssertionError("Instantiator not yet configured");
                }
                for (Contribution contribution : entry.getValue()) {
                    URI exportUri = contribution.getUri();
                    URI location = imprt.getLocation();
                    if (location != null && location.equals(exportUri)) {
                        // location specified, resolve to exact contribution
                        ContributionWire<Import, Export> wire = instantiatorRegistry.instantiate(imprt, export, uri, exportUri);
                        wires.add(wire);
                        return wires;
                    } else if (location == null) {
                        ContributionWire<Import, Export> wire = instantiatorRegistry.instantiate(imprt, export, uri, exportUri);
                        wires.add(wire);
                        if (!imprt.isMultiplicity()) {
                            return wires;
                        }
                    }
                }
            }
        }
        if (wires.isEmpty()) {
            throw new UnresolvedImportException(imprt.toString());
        }
        return wires;
    }

    public Set<Contribution> resolveDependentContributions(URI uri) {
        Set<Contribution> dependents = new HashSet<Contribution>();
        for (Contribution entry : cache.values()) {
            List<ContributionWire<?, ?>> contributionWires = entry.getWires();
            for (ContributionWire<?, ?> wire : contributionWires) {
                if (uri.equals(wire.getExportContributionUri())) {
                    dependents.add(entry);
                    break;
                }
            }
        }
        return dependents;
    }

    public List<Contribution> resolveExtensionProviders(String name) {
        List<Contribution> providers = new ArrayList<Contribution>();
        for (Contribution contribution : cache.values()) {
            for (String extend : contribution.getManifest().getExtends()) {
                if (extend.equals(name)) {
                    providers.add(contribution);
                    break;
                }
            }
        }
        return providers;
    }

    public List<Contribution> resolveExtensionPoints(String name) {
        List<Contribution> extensionPoints = new ArrayList<Contribution>();
        for (Contribution contribution : cache.values()) {
            for (String extensionPoint : contribution.getManifest().getExtensionPoints()) {
                if (extensionPoint.equals(name)) {
                    extensionPoints.add(contribution);
                    break;
                }
            }
        }
        return extensionPoints;
    }

    public Set<Contribution> resolveCapabilities(Contribution contribution) {
        Set<Contribution> extensions = new HashSet<Contribution>();
        return resolveCapabilities(contribution, extensions);
    }

    public Set<Contribution> resolveCapability(String capability) {
        Set<Contribution> extensions = new HashSet<Contribution>();
        for (Contribution entry : cache.values()) {
            Capability key = new Capability(capability);
            if (entry.getManifest().getProvidedCapabilities().contains(key) && !extensions.contains(entry)) {
                extensions.add(entry);
                resolveCapabilities(entry, extensions);
            }
        }
        return extensions;
    }

    private Set<Contribution> resolveCapabilities(Contribution contribution, Set<Contribution> extensions) {
        Set<Capability> required = contribution.getManifest().getRequiredCapabilities();
        for (Capability capability : required) {
            for (Contribution entry : cache.values()) {
                if (entry.getManifest().getProvidedCapabilities().contains(capability) && !extensions.contains(entry)) {
                    extensions.add(entry);
                    resolveCapabilities(entry, extensions);
                }
            }
        }
        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            Contribution imported = cache.get(wire.getExportContributionUri());
            if (imported.getManifest().isExtension()
                    && !extensions.contains(imported)
                    && !imported.getUri().equals(Names.HOST_CONTRIBUTION)
                    && !imported.getUri().equals(Names.BOOT_CONTRIBUTION)) {
                // only add to the list of extensions if the imported contribution is an extension, is not already present,
                // and is not the host or boot classloaders.
                extensions.add(imported);
            }
            // recurse for the imported contribution
            resolveCapabilities(imported, extensions);
        }
        for (URI uri : contribution.getResolvedExtensionProviders()) {
            Contribution provider = cache.get(uri);
            if (!extensions.contains(provider)) {
                extensions.add(provider);
            }
            // TODO figure out how to recurse up providers without introducing a cycle
            //  resolveCapabilities(provider, extensions);
        }
        return extensions;
    }


    @SuppressWarnings({"unchecked"})
    private <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolveInternal(Contribution contribution,
                                                                                             Class<V> type,
                                                                                             S symbol,
                                                                                             IntrospectionContext context) throws StoreException {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol().equals(symbol)) {
                    if (ResourceState.PROCESSED != resource.getState() && context == null) {
                        String identifier = resource.getSource().getSystemId();
                        throw new AssertionError("Resource not resolved: " + identifier);
                    } else if (ResourceState.UNPROCESSED == resource.getState() && context != null) {
                        try {
                            processorRegistry.processResource(resource, context);
                        } catch (ContributionException e) {
                            String identifier = resource.getSource().getSystemId();
                            throw new StoreException("Error resolving resource: " + identifier, e);
                        }
                    }
                    Object val = element.getValue();
                    if (!type.isInstance(val)) {
                        throw new IllegalArgumentException("Invalid type for symbol. Expected: " + type + " was: " + val);
                    }
                    return (ResourceElement<S, V>) element;
                }
            }
        }
        return null;
    }

    /**
     * Adds the contribution exports to the cached list of exports for the domain
     *
     * @param contribution the contribution containing the exports to add
     */
    private void addToExports(Contribution contribution) {
        List<Export> exports = contribution.getManifest().getExports();
        if (exports.size() > 0) {
            for (Export export : exports) {
                Map<Export, List<Contribution>> map = exportsToContributionCache.get(export.getType());
                if (map == null) {
                    map = new ConcurrentHashMap<Export, List<Contribution>>();
                    exportsToContributionCache.put(export.getType(), map);
                    List<Contribution> contributions = new ArrayList<Contribution>();
                    contributions.add(contribution);
                    map.put(export, contributions);
                } else {
                    List<Contribution> contributions = map.get(export);
                    if (contributions == null) {
                        contributions = new ArrayList<Contribution>();
                        map.put(export, contributions);
                    }
                    contributions.add(contribution);
                }
            }
        }
    }

}
