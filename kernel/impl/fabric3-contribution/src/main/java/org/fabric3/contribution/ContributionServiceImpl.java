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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.ArtifactValidationFailure;
import org.fabric3.api.host.contribution.ContributionOrder;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.contribution.ValidationException;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.api.host.failure.ValidationUtils;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default ContributionService implementation.
 */
@EagerInit
public class ContributionServiceImpl implements ContributionService {
    private ProcessorRegistry processorRegistry;
    private MetaDataStore metaDataStore;
    private ContributionLoader contributionLoader;
    private ContentTypeResolver contentTypeResolver;
    private DependencyResolver dependencyResolver;
    private ContributionServiceMonitor monitor;
    private List<ContributionServiceListener> listeners;

    public ContributionServiceImpl(@Reference ProcessorRegistry processorRegistry,
                                   @Reference MetaDataStore metaDataStore,
                                   @Reference ContributionLoader contributionLoader,
                                   @Reference ContentTypeResolver contentTypeResolver,
                                   @Reference DependencyResolver dependencyResolver,
                                   @Monitor ContributionServiceMonitor monitor) {
        this.processorRegistry = processorRegistry;
        this.metaDataStore = metaDataStore;
        this.contributionLoader = contributionLoader;
        this.contentTypeResolver = contentTypeResolver;
        this.dependencyResolver = dependencyResolver;
        this.monitor = monitor;
        listeners = new ArrayList<>();
    }

    @Reference(required = false)
    public void setListeners(List<ContributionServiceListener> listeners) {
        this.listeners = listeners;
    }


    public Set<URI> getContributions() {
        Set<Contribution> contributions = metaDataStore.getContributions();
        Set<URI> uris = new HashSet<>(contributions.size());
        uris.addAll(contributions.stream().map(Contribution::getUri).collect(Collectors.toList()));
        return uris;
    }

    public boolean exists(URI uri) {
        return metaDataStore.find(uri) != null;
    }

    public List<Deployable> getDeployables(URI uri) {
        Contribution contribution = find(uri);
        List<Deployable> list = new ArrayList<>();
        if (contribution.getManifest() != null) {
            list.addAll(contribution.getManifest().getDeployables().stream().collect(Collectors.toList()));
        }
        return list;
    }

    public URI store(ContributionSource contributionSource) {
        Contribution contribution = create(contributionSource);
        metaDataStore.store(contribution);
        for (ContributionServiceListener listener : listeners) {
            listener.onStore(contribution);
        }
        return contribution.getUri();
    }

    public List<URI> store(List<ContributionSource> contributionSources) {
        List<URI> uris = new ArrayList<>();
        for (ContributionSource contributionSource : contributionSources) {
            URI uri = store(contributionSource);
            uris.add(uri);
        }
        return uris;
    }

    public void install(URI uri) {
        install(Collections.singletonList(uri));
    }

    public List<URI> install(List<URI> uris) {
        List<Contribution> contributions = new ArrayList<>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = find(uri);
            contributions.add(contribution);
        }
        return installInOrder(contributions);
    }

    public void uninstall(URI uri) {
        Contribution contribution = find(uri);
        uninstall(contribution);
    }

    public void uninstall(List<URI> uris) {
        List<Contribution> contributions = new ArrayList<>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = find(uri);
            contributions.add(contribution);
        }
        contributions = dependencyResolver.orderForUninstall(contributions);
        contributions.forEach(this::uninstall);
    }

    public void remove(URI uri) {
        Contribution contribution = find(uri);
        if (contribution.getState() != ContributionState.STORED) {
            throw new Fabric3Exception("Contribution must first be uninstalled: " + uri);
        }
        metaDataStore.remove(uri);
        for (ContributionServiceListener listener : listeners) {
            listener.onRemove(contribution);
        }
    }

    public void remove(List<URI> uris) {
        uris.forEach(this::remove);
    }

    public ContributionOrder processManifests(List<ContributionSource> contributionSources) {
        List<Contribution> contributions = new ArrayList<>();
        for (ContributionSource contributionSource : contributionSources) {
            // store the contributions
            Contribution contribution = create(contributionSource);
            metaDataStore.store(contribution);
            for (ContributionServiceListener listener : listeners) {
                listener.onStore(contribution);
            }
            contributions.add(contribution);
        }
        return introspectManifests(contributions);
    }

    public void processContents(URI uri) {
        Contribution contribution = find(uri);
        try {
            ClassLoader loader = contributionLoader.load(contribution);
            // continue processing the contributions. As they are ordered, dependencies will resolve correctly
            processContents(contribution, loader);
            contribution.setState(ContributionState.INSTALLED);
            for (ContributionServiceListener listener : listeners) {
                listener.onInstall(contribution);
            }
        } catch (Fabric3Exception e) {
            try {
                revertInstall(Collections.singletonList(contribution));
            } catch (RuntimeException ex) {
                monitor.error("Error reverting deployment", ex);
            }
            throw e;
        }
        String description = contribution.getManifest().getDescription();
        if (description != null) {
            monitor.installed(description);
        }
    }

    private ContributionOrder introspectManifests(List<Contribution> contributions) {
        ContributionOrder order = new ContributionOrder();
        for (Contribution contribution : contributions) {
            if (ContributionState.STORED != contribution.getState()) {
                throw new Fabric3Exception("Contribution is already installed: " + contribution.getUri());
            }
        }
        // process any SCA manifest information, including imports and exports
        contributions.forEach(this::processManifest);
        // order the contributions based on their dependencies
        contributions = dependencyResolver.resolve(contributions);
        for (Contribution contribution : contributions) {
            boolean requiresLoad = false;
            ContributionManifest manifest = contribution.getManifest();
            for (Capability capability : manifest.getRequiredCapabilities()) {
                if (capability.requiresLoad()) {
                    requiresLoad = true;
                    break;
                }
            }
            if (requiresLoad) {
                order.addIsolatedContribution(contribution.getUri());
            } else {
                order.addBaseContribution(contribution.getUri());
            }
        }
        return order;
    }

    /**
     * Resolves a contribution by its URI.
     *
     * @param uri the contribution URI
     * @return the contribution
     * @ if the contribution does not exist
     */
    private Contribution find(URI uri) {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            throw new Fabric3Exception("Contribution not found: " + uri);
        }
        return contribution;
    }

    /**
     * Installs a collection of contributions in order of their dependencies.
     *
     * @param contributions the contributions
     * @return the ordered list of contribution URIs
     * @ if there is an error installing the contributions
     */
    private List<URI> installInOrder(List<Contribution> contributions) {
        for (Contribution contribution : contributions) {
            if (ContributionState.STORED != contribution.getState()) {
                throw new Fabric3Exception("Contribution is already installed: " + contribution.getUri());
            }
        }
        // process any SCA manifest information, including imports and exports
        contributions.forEach(this::processManifest);
        // order the contributions based on their dependencies
        contributions = dependencyResolver.resolve(contributions);
        try {
            for (Contribution contribution : contributions) {
                ClassLoader loader = contributionLoader.load(contribution);
                // continue processing the contributions. As they are ordered, dependencies will resolve correctly
                processContents(contribution, loader);
                contribution.setState(ContributionState.INSTALLED);
                for (ContributionServiceListener listener : listeners) {
                    listener.onInstall(contribution);
                }
            }
        } catch (Fabric3Exception e) {
            try {
                revertInstall(contributions);
            } catch (RuntimeException ex) {
                monitor.error("Error reverting deployment", ex);
            }
            throw e;
        }
        List<URI> uris = new ArrayList<>(contributions.size());
        for (Contribution contribution : contributions) {
            URI uri = contribution.getUri();
            uris.add(uri);
            String description = contribution.getManifest().getDescription();
            if (description != null) {
                monitor.installed(description);
            } else if (!contribution.getManifest().isExtension()) {
                monitor.installed(uri.toString());
            }
        }
        return uris;
    }

    private void revertInstall(List<Contribution> contributions) {
        ListIterator<Contribution> iterator = contributions.listIterator(contributions.size());
        while (iterator.hasPrevious()) {
            Contribution contribution = iterator.previous();
            try {
                if (ContributionState.INSTALLED == contribution.getState()) {
                    uninstall(contribution);
                }
                contributionLoader.unload(contribution);
                remove(contribution.getUri());
            } catch (Fabric3Exception ex) {
                monitor.error("Error reverting installation: " + contribution.getUri(), ex);
            }
        }
    }

    private void uninstall(Contribution contribution) {
        URI uri = contribution.getUri();
        if (contribution.getState() != ContributionState.INSTALLED) {
            throw new Fabric3Exception("Contribution not installed: " + uri);
        }
        if (contribution.isLocked()) {
            throw new Fabric3Exception("Contribution is currently in use by a deployment: " + uri);
        }
        // unload from memory
        contributionLoader.unload(contribution);
        contribution.setState(ContributionState.STORED);
        for (ContributionServiceListener listener : listeners) {
            listener.onUninstall(contribution);
        }
        String description = contribution.getManifest().getDescription();
        if (description != null) {
            monitor.uninstalled(description);
        } else if (!contribution.getManifest().isExtension()) {
            monitor.uninstalled(uri.toString());
        }
    }

    /**
     * Processes the contribution manifest.
     *
     * @param contribution the contribution
     * @ if there is an error during introspection
     */
    private void processManifest(Contribution contribution) {
        IntrospectionContext context = new DefaultIntrospectionContext();
        processorRegistry.processManifest(contribution, context);
        if (context.hasErrors()) {
            URI uri = contribution.getUri();
            ArtifactValidationFailure failure = new ArtifactValidationFailure(uri, "the contribution manifest (sca-contribution.xml)");
            failure.addFailures(context.getErrors());
            List<ValidationFailure> failures = new ArrayList<>();
            failures.add(failure);
            ArtifactValidationFailure warning = new ArtifactValidationFailure(uri, "the contribution manifest (sca-contribution.xml)");
            warning.addFailures(context.getWarnings());
            List<ValidationFailure> warnings = new ArrayList<>();
            warnings.add(warning);
            throw new ValidationException(failures, warnings);
        }
        for (ContributionServiceListener listener : listeners) {
            listener.onProcessManifest(contribution);
        }

    }

    /**
     * Processes contribution contents. This assumes all dependencies are installed and can be resolved.
     *
     * @param contribution the contribution to process
     * @param loader       the classloader to load resources in
     * @ if an error occurs during processing
     */
    private void processContents(Contribution contribution, ClassLoader loader) {
        URI contributionUri = contribution.getUri();
        IntrospectionContext context = new DefaultIntrospectionContext(contributionUri, loader);
        processorRegistry.indexContribution(contribution, context);
        if (context.hasErrors()) {
            throw new ValidationException(context.getErrors(), context.getWarnings());
        } else if (context.hasWarnings()) {
            // there were just warnings, report them
            monitor.contributionWarnings(ValidationUtils.outputWarnings(context.getWarnings()));
        }
        metaDataStore.store(contribution);
        context = new DefaultIntrospectionContext(contributionUri, loader);
        processorRegistry.processContribution(contribution, context);
        validateContribution(contribution, context);
        if (context.hasErrors()) {
            throw new ValidationException(context.getErrors(), context.getWarnings());
        } else if (context.hasWarnings()) {
            // there were just warnings, report them
            monitor.contributionWarnings(ValidationUtils.outputWarnings(context.getWarnings()));
        }
        addDeployableEntries(contribution);
    }

    /**
     * Performs final validation on a contribution.
     *
     * @param contribution the contribution to validate
     * @param context      the validation context
     */
    private void validateContribution(Contribution contribution, IntrospectionContext context) {
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            QName name = deployable.getName();
            QNameSymbol symbol = new QNameSymbol(name);
            boolean found = false;
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        found = true;
                    }
                }
            }
            if (!found) {
                URI uri = contribution.getUri();
                InvalidDeployable failure = new InvalidDeployable("Deployable composite " + name + " not found in " + uri, name);
                context.addError(failure);
            }

        }
    }

    /**
     * Adds deployable entries for composites that are defined as deployables but do not have an explicit entry in the manifest.
     *
     * @param contribution the contribution
     */
    private void addDeployableEntries(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        for (Resource resource : contribution.getResources()) {
            resource.getResourceElements().stream().filter(element -> element.getValue() instanceof Composite).forEach(element -> {
                Composite composite = (Composite) element.getValue();
                if (composite.isDeployable()) {
                    Deployable deployable = new Deployable(composite.getName(), composite.getModes(), composite.getEnvironments());
                    if (!manifest.getDeployables().contains(deployable)) {
                        manifest.getDeployables().add(deployable);
                    }
                }
            });
        }
    }

    /**
     * Creates a contribution in the archive store
     *
     * @param contributionSource the contribution source
     * @return the contribution
     * @ if an error occurs during the store operation
     */
    private Contribution create(ContributionSource contributionSource) {
        URI contributionUri = contributionSource.getUri();
        if (metaDataStore.find(contributionUri) != null) {
            throw new Fabric3Exception("Contribution already exists: " + contributionUri);
        }
        Source source;
        URL locationUrl = contributionSource.getLocation();
        // reuse the source as the contribution is locally resolvable
        source = contributionSource.getSource();
        String type = contributionSource.getContentType();
        if (type == null && locationUrl == null) {
            throw new Fabric3Exception("Content type could not be determined for contribution: " + contributionUri);
        }
        if (type == null) {
            type = contentTypeResolver.getContentType(locationUrl);
        }
        long timestamp = contributionSource.getTimestamp();
        return new Contribution(contributionUri, source, locationUrl, timestamp, type);
    }

}
