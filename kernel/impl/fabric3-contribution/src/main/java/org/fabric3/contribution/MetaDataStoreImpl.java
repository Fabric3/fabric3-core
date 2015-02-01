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
package org.fabric3.contribution;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistry;
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
import org.oasisopen.sca.annotation.Reference;

/**
 * Default MetaDataStore implementation.
 */
public class MetaDataStoreImpl implements MetaDataStore {
    private ProcessorRegistry processorRegistry;
    private ContributionWireInstantiatorRegistry instantiatorRegistry;

    private Map<URI, Contribution> cache = new ConcurrentHashMap<>();

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

    public void store(Contribution contribution) {
        cache.put(contribution.getUri(), contribution);
    }

    public Contribution find(URI contributionUri) {
        return cache.get(contributionUri);
    }

    public Set<Contribution> getContributions() {
        return new HashSet<>(cache.values());
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
                        if (ResourceState.UNPROCESSED == resource.getState()) {
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

    public <S extends Symbol, V extends Serializable> ResourceElement<S, V> find(URI uri, Class<V> type, S symbol) {
        return resolve(uri, type, symbol, null);
    }

    public <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(URI uri, Class<V> type, S symbol, IntrospectionContext context) {
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new Fabric3Exception("Contribution not found: " + identifier);
        }

        return resolve(contribution, type, symbol, context);
    }

    @SuppressWarnings({"unchecked"})
    public <V extends Serializable> List<ResourceElement<?, V>> resolve(URI uri, Class<V> type) {
        Contribution contribution = find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new Fabric3Exception("Contribution not found: " + identifier);
        }
        List<ResourceElement<?, V>> artifacts = new ArrayList<>();
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                Object value = element.getValue();
                if (value == null) {
                    continue;
                }
                if (value.getClass().isAssignableFrom(type)) {
                    artifacts.add((ResourceElement<?, V>) element);
                }
            }
        }

        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            URI exportingUri = wire.getExportContributionUri();
            Contribution exporting = find(exportingUri);
            for (Resource resource : exporting.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (!wire.resolves(element.getSymbol())) {
                        // artifact not visible from the importing contribution
                        continue;
                    }
                    Object value = element.getValue();
                    if (value == null) {
                        continue;
                    }
                    if (value.getClass().isAssignableFrom(type)) {
                        artifacts.add((ResourceElement<?, V>) element);
                    }
                }
            }
        }
        return artifacts;
    }

    public List<Contribution> resolve(URI uri, Import imprt) {
        List<Contribution> resolved = new ArrayList<>();
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

    public List<ContributionWire<?, ?>> resolveContributionWires(URI uri, Import imprt) {
        List<ContributionWire<?, ?>> wires = new ArrayList<>();
        for (Map.Entry<URI, Export> entry : imprt.getResolved().entrySet()) {
            ContributionWire<Import, Export> wire = instantiatorRegistry.instantiate(imprt, entry.getValue(), uri, entry.getKey());
            wires.add(wire);
        }
        if (wires.isEmpty()) {
            throw new Fabric3Exception(imprt.toString());
        }
        return wires;
    }

    public Set<Contribution> resolveDependentContributions(URI uri) {
        Set<Contribution> dependents = new HashSet<>();
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
        List<Contribution> providers = new ArrayList<>();
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
        List<Contribution> extensionPoints = new ArrayList<>();
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
        Set<Contribution> extensions = new HashSet<>();
        return resolveCapabilities(contribution, extensions);
    }

    public Set<Contribution> resolveCapability(String capability) {
        Set<Contribution> extensions = new HashSet<>();
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
                                                                                     IntrospectionContext context) {
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

    private Set<Contribution> resolveCapabilities(Contribution contribution, Set<Contribution> extensions) {
        Set<Capability> required = contribution.getManifest().getRequiredCapabilities();
        for (Capability capability : required) {
            cache.values().stream().filter(entry -> entry.getManifest().getProvidedCapabilities().contains(capability) && !extensions.contains(entry)).forEach(
                    entry -> {
                        extensions.add(entry);
                        resolveCapabilities(entry, extensions);
                    });
        }
        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            Contribution imported = cache.get(wire.getExportContributionUri());
            if (imported.getManifest().isExtension() && !extensions.contains(imported) && !imported.getUri().equals(Names.HOST_CONTRIBUTION)
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
                                                                                             IntrospectionContext context) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol().equals(symbol)) {
                    if (ResourceState.UNPROCESSED == resource.getState() && context == null) {
                        String identifier = resource.getSource().getSystemId();
                        throw new AssertionError("Resource not resolved: " + identifier);
                    } else if (ResourceState.UNPROCESSED == resource.getState() && context != null) {
                        processorRegistry.processResource(resource, context);
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

}
