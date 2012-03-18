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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.annotation.Reference;

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
import org.fabric3.spi.contribution.ReferenceIntrospector;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementUpdater;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default MetaDataStore implementation.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImpl implements MetaDataStore {
    private ProcessorRegistry processorRegistry;
    private ContributionWireInstantiatorRegistry instantiatorRegistry;
    private Map<String, ReferenceIntrospector> referenceIntrospectors = new HashMap<String, ReferenceIntrospector>();
    private Map<String, ResourceElementUpdater<?>> updaters = new HashMap<String, ResourceElementUpdater<?>>();

    private Map<URI, Contribution> cache = new ConcurrentHashMap<URI, Contribution>();

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

    /**
     * Sets the reference introspectors, keyed by class name. Class name is used since singleton components only support String-based keys.
     *
     * @param introspectors the introspectors.
     */
    @Reference(required = false)
    public void setReferenceIntrospectors(Map<String, ReferenceIntrospector> introspectors) {
        this.referenceIntrospectors = introspectors;
    }

    /**
     * Sets the element updaters, keyed by class name. Class name is used since singleton components only support String-based keys.
     *
     * @param updaters the updaters.
     */
    @Reference(required = false)
    public void setUpdaters(Map<String, ResourceElementUpdater<?>> updaters) {
        this.updaters = updaters;
    }

    public void store(Contribution contribution) throws StoreException {
        cache.put(contribution.getUri(), contribution);
    }

    public Contribution find(URI contributionUri) {
        return cache.get(contributionUri);
    }

    public Set<Contribution> getContributions() {
        return new HashSet<Contribution>(cache.values());
    }

    public void remove(URI contributionUri) {
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

    public <S extends Symbol> Set<ResourceElement<S, ?>> findReferences(URI uri, S symbol) throws StoreException {
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new ContributionResolutionException("Contribution not found: " + identifier, identifier);
        }

        ResourceElement<S, ?> referred = find(uri, Serializable.class, symbol);
        if (referred == null) {
            return Collections.emptySet();
        }
        ReferenceIntrospector introspector = referenceIntrospectors.get(referred.getValue().getClass().getName());
        if (introspector == null) {
            return Collections.emptySet();
        }

        Set<ResourceElement<S, ?>> elements = new HashSet<ResourceElement<S, ?>>();

        // check the contribution for referring artifacts
        findReferences(contribution, referred, elements, introspector);

        // check dependent contributions
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
            findReferences(resolved, referred, elements, introspector);
        }
        return elements;
    }

    public <V extends Serializable> void update(URI uri, V value) throws StoreException {
        ResourceElementUpdater<V> updater = getUpdater(uri, value);
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new ContributionResolutionException("Contribution not found: " + identifier, identifier);
        }
        Set<Contribution> dependentContributions = resolveDependentContributions(uri);
        updater.update(value, contribution, dependentContributions);
    }

    public <V extends Serializable> void remove(URI uri, V value) throws StoreException {
        ResourceElementUpdater<V> updater = getUpdater(uri, value);
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new ContributionResolutionException("Contribution not found: " + identifier, identifier);
        }
        Set<Contribution> dependentContributions = resolveDependentContributions(uri);
        updater.remove(value, contribution, dependentContributions);
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

    @SuppressWarnings({"unchecked"})
    public <V extends Serializable> List<ResourceElement<?, V>> resolve(URI uri, Class<V> type) throws StoreException {
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new ContributionResolutionException("Contribution not found: " + identifier, identifier);
        }
        List<ResourceElement<?, V>> artifacts = new ArrayList<ResourceElement<?, V>>();
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                Object value = element.getValue();
                if (value == null) {
                    continue;
                }
                if (value.getClass().isAssignableFrom(type)){
                    artifacts.add((ResourceElement<?, V>) element);
                }
            }
        }

        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            URI exportingUri = wire.getExportContributionUri();
            Contribution exporting = find(exportingUri);
            for (Resource resource : exporting.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (!wire.resolves(element.getSymbol())){
                        // artifact not visible from the importing contribution
                        continue;
                    }
                    Object value = element.getValue();
                    if (value == null) {
                        continue;
                    }
                    if (value.getClass().isAssignableFrom(type)){
                        artifacts.add((ResourceElement<?, V>) element);
                    }
                }
            }
        }
        return artifacts;
    }

    public List<Contribution> resolve(URI uri, Import imprt) {
        List<Contribution> resolved = new ArrayList<Contribution>();
        if (!imprt.getResolved().isEmpty()) {
            // already resolved
            for (URI exportUri : imprt.getResolved().keySet()) {
                Contribution contribution = cache.get(exportUri);
                if (contribution == null) {
                    throw new AssertionError("Contribution not found: " + contribution);
                }
                resolved.add(contribution);
            }
            return resolved;
        }

        URI location = imprt.getLocation();

        for (Contribution contribution : cache.values()) {
            for (Export export : contribution.getManifest().getExports()) {
                if (export.match(imprt)) {
                    if (location != null) {
                        // location is specified, resolve to the explicit contribution with that export
                        if (location.equals(contribution.getUri())) {
                            resolved.add(contribution);
                            imprt.addResolved(contribution.getUri(), export);
                            export.resolve();
                            return resolved;   // finished, since location is used to specify exactly one contribution
                        }
                    } else {
                        if (!uri.equals(contribution.getUri())) {
                            resolved.add(contribution);
                            imprt.addResolved(contribution.getUri(), export);
                            export.resolve();
                        }
                    }

                }
            }
        }
        return resolved;
    }

    public List<ContributionWire<?, ?>> resolveContributionWires(URI uri, Import imprt) throws UnresolvedImportException {
        List<ContributionWire<?, ?>> wires = new ArrayList<ContributionWire<?, ?>>();
        for (Map.Entry<URI, Export> entry : imprt.getResolved().entrySet()) {
            ContributionWire<Import, Export> wire = instantiatorRegistry.instantiate(imprt, entry.getValue(), uri, entry.getKey());
            wires.add(wire);
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

    @SuppressWarnings({"unchecked"})
    private <S extends Symbol> void findReferences(Contribution contribution,
                                                   ResourceElement<S, ?> referred,
                                                   Set<ResourceElement<S, ?>> elements,
                                                   ReferenceIntrospector introspector) {

        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (introspector.references(referred, element)) {
                    elements.add((ResourceElement<S, ?>) element);
                }
            }
        }
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

    @SuppressWarnings({"unchecked"})
    private <V extends Serializable> ResourceElementUpdater<V> getUpdater(URI uri, V value) throws ContributionUpdateException {
        String clazz = value.getClass().getName();
        ResourceElementUpdater<V> updater = (ResourceElementUpdater<V>) updaters.get(clazz);
        if (updater == null) {
            String identifier = uri.toString();
            throw new ContributionUpdateException("Updater not found: " + identifier);
        }
        return updater;
    }

}
