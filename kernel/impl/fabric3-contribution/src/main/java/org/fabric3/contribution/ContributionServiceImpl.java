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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.contribution.ContributionLockedException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.DuplicateContributionException;
import org.fabric3.host.contribution.DuplicateProfileException;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.contribution.UninstallException;
import org.fabric3.host.contribution.UpdateException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.repository.RepositoryException;
import org.fabric3.host.repository.Repository;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.validation.InvalidContributionException;
import org.fabric3.spi.introspection.validation.ValidationUtils;

/**
 * Default ContributionService implementation
 *
 * @version $Rev$ $Date$
 */
@Service(ContributionService.class)
@EagerInit
public class ContributionServiceImpl implements ContributionService {
    private ProcessorRegistry processorRegistry;
    private Repository repository;
    private MetaDataStore metaDataStore;
    private ContributionLoader contributionLoader;
    private ContentTypeResolver contentTypeResolver;
    private DependencyService dependencyService;
    private ContributionServiceMonitor monitor;
    private List<ContributionServiceListener> listeners;

    public ContributionServiceImpl(@Reference ProcessorRegistry processorRegistry,
                                   @Reference MetaDataStore metaDataStore,
                                   @Reference ContributionLoader contributionLoader,
                                   @Reference ContentTypeResolver contentTypeResolver,
                                   @Reference DependencyService dependencyService,
                                   @Monitor ContributionServiceMonitor monitor) {
        this.processorRegistry = processorRegistry;
        this.metaDataStore = metaDataStore;
        this.contributionLoader = contributionLoader;
        this.contentTypeResolver = contentTypeResolver;
        this.dependencyService = dependencyService;
        this.monitor = monitor;
        listeners = new ArrayList<ContributionServiceListener>();
    }

    @Reference(required = false)
    public void setListeners(List<ContributionServiceListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * Lazily injects the repository. Some environments may inject the repository via an extension loaded after bootstrap.
     *
     * @param repository the store to inject
     */
    @Reference(required = false)
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public URI store(ContributionSource source) throws StoreException {
        Contribution contribution = persist(source);
        metaDataStore.store(contribution);
        for (ContributionServiceListener listener : listeners) {
            listener.onStore(contribution);
        }
        return contribution.getUri();
    }

    public void install(URI uri) throws InstallException {
        Contribution contribution = metaDataStore.find(uri);
        install(contribution);
        // update the store with changes
        try {
            metaDataStore.store(contribution);
        } catch (StoreException e) {
            throw new InstallException(e);
        }
        for (ContributionServiceListener listener : listeners) {
            listener.onInstall(contribution);
        }
    }

    public void install(List<URI> uris) throws InstallException, ContributionNotFoundException {
        List<Contribution> contributions = new ArrayList<Contribution>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = metaDataStore.find(uri);
            if (contribution == null) {
                throw new ContributionNotFoundException("Contribution not found: " + uri);
            }
            contributions.add(contribution);
        }
        installInOrder(contributions);
    }

    public List<URI> contribute(List<ContributionSource> sources) throws ContributionException {
        List<Contribution> contributions = new ArrayList<Contribution>(sources.size());
        for (ContributionSource source : sources) {
            // store the contributions
            Contribution contribution = persist(source);
            for (ContributionServiceListener listener : listeners) {
                listener.onStore(contribution);
            }
            contributions.add(contribution);
        }
        return installInOrder(contributions);
    }


    public URI contribute(ContributionSource source) throws ContributionException {
        Contribution contribution = persist(source);
        for (ContributionServiceListener listener : listeners) {
            listener.onStore(contribution);
        }
        install(contribution);
        for (ContributionServiceListener listener : listeners) {
            listener.onInstall(contribution);
        }
        return contribution.getUri();
    }

    public boolean exists(URI uri) {
        return metaDataStore.find(uri) != null;
    }

    public void update(ContributionSource source) throws UpdateException, ContributionNotFoundException {
        URI uri = source.getUri();
        byte[] checksum = source.getChecksum();
        long timestamp = source.getTimestamp();
        InputStream is = null;
        try {
            is = source.getSource();
            update(uri, checksum, timestamp);
        } catch (IOException e) {
            throw new UpdateException("Contribution error", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                monitor.error("Error closing stream", e);
            }
        }
    }

    public long getContributionTimestamp(URI uri) {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            return -1;
        }
        return contribution.getTimestamp();
    }

    public Set<URI> getContributions() {
        Set<Contribution> contributions = metaDataStore.getContributions();
        Set<URI> uris = new HashSet<URI>(contributions.size());
        for (Contribution contribution : contributions) {
            uris.add(contribution.getUri());
        }
        return uris;
    }

    public boolean profileExists(URI uri) {
        for (Contribution contribution : metaDataStore.getContributions()) {
            if (contribution.getProfiles().contains(uri)) {
                return true;
            }
        }
        return false;
    }

    public void installProfile(URI uri) throws InstallException, ContributionNotFoundException {
        List<Contribution> toInstall = new ArrayList<Contribution>();
        for (Contribution contribution : metaDataStore.getContributions()) {
            if (contribution.getProfiles().contains(uri) && ContributionState.STORED == contribution.getState()) {
                toInstall.add(contribution);
            }
        }
        if (toInstall.isEmpty()) {
            throw new ContributionNotFoundException("Profile not found: " + uri);
        }
        installInOrder(toInstall);
    }

    public void uninstallProfile(URI uri) throws UninstallException, ContributionNotFoundException {
        List<Contribution> toUninstall = new ArrayList<Contribution>();
        for (Contribution contribution : metaDataStore.getContributions()) {
            List<URI> profiles = contribution.getProfiles();
            if (profiles.contains(uri)) {
                if (profiles.size() == 1) {
                    ContributionState state = contribution.getState();
                    if (ContributionState.INSTALLED != state) {
                        throw new UninstallException("Contribution not in installed state: " + state);
                    }
                    toUninstall.add(contribution);
                }
            }
        }
        toUninstall = dependencyService.orderForUninstall(toUninstall);
        for (Contribution contribution : toUninstall) {
            uninstall(contribution.getUri());
        }
    }

    public void removeProfile(URI uri) throws RemoveException, ContributionNotFoundException {
        List<Contribution> toRemove = new ArrayList<Contribution>();
        for (Contribution contribution : metaDataStore.getContributions()) {
            List<URI> profiles = contribution.getProfiles();
            if (profiles.contains(uri)) {
                if (profiles.size() == 1) {
                    ContributionState state = contribution.getState();
                    if (ContributionState.STORED != state) {
                        throw new RemoveException("Contribution not in stored state: " + state);
                    }
                    toRemove.add(contribution);
                } else {
                    contribution.removeProfile(uri);
                }
            }
        }
        for (Contribution contribution : toRemove) {
            remove(contribution.getUri());
        }
    }

    public void registerProfile(URI profileUri, List<URI> contributionUris) throws DuplicateProfileException {
        if (profileExists(profileUri)) {
            throw new DuplicateProfileException("Profile already installed: " + profileUri);
        }
        for (URI contributionUri : contributionUris) {
            Contribution contribution = metaDataStore.find(contributionUri);
            if (contribution == null) {
                throw new AssertionError("Contribution not found: " + contributionUri);
            }
            List<URI> profiles = contribution.getProfiles();
            if (!profiles.contains(profileUri)) {
                profiles.add(profileUri);
                for (ContributionServiceListener listener : listeners) {
                    listener.onUpdate(contribution);
                }
            }
        }
    }

    public List<Deployable> getDeployables(URI contributionUri) throws ContributionNotFoundException {
        Contribution contribution = find(contributionUri);
        List<Deployable> list = new ArrayList<Deployable>();
        if (contribution.getManifest() != null) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                list.add(deployable);
            }
        }
        return list;
    }

    public void uninstall(URI uri) throws UninstallException, ContributionNotFoundException {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution does not exist:" + uri);
        }
        uninstall(contribution);
    }

    private void uninstall(Contribution contribution) throws UninstallException, ContributionNotFoundException {
        URI uri = contribution.getUri();
        if (contribution.getState() != ContributionState.INSTALLED) {
            throw new UninstallException("Contribution not installed: " + uri);
        }
        if (contribution.isLocked()) {
            Set<QName> deployables = contribution.getLockOwners();
            throw new ContributionLockedException("Contribution is currently in use by a deployment: " + uri, uri, deployables);
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
        }
    }

    public void uninstall(List<URI> uris) throws UninstallException, ContributionNotFoundException {
        List<Contribution> contributions = new ArrayList<Contribution>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = metaDataStore.find(uri);
            if (contribution == null) {
                throw new ContributionNotFoundException("Contribution does not exist:" + uri);
            }
            contributions.add(contribution);
        }
        contributions = dependencyService.orderForUninstall(contributions);
        for (Contribution contribution : contributions) {
            uninstall(contribution);
        }
    }

    public void remove(URI uri) throws RemoveException, ContributionNotFoundException {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found:" + uri);
        }
        if (contribution.getState() != ContributionState.STORED) {
            throw new RemoveException("Contribution must first be uninstalled: " + uri);
        }
        metaDataStore.remove(uri);
        try {
            getRepository().remove(uri);
        } catch (RepositoryException e) {
            throw new RemoveException("Error removing contribution archive", e);
        }
        for (ContributionServiceListener listener : listeners) {
            listener.onRemove(contribution);
        }
    }

    public void remove(List<URI> uris) throws ContributionNotFoundException, RemoveException {
        for (URI uri : uris) {
            remove(uri);
        }
    }

    public List<URI> getContributionsInProfile(URI uri) {
        List<URI> profileContributions = new ArrayList<URI>();
        Set<Contribution> contributions = metaDataStore.getContributions();
        for (Contribution contribution : contributions) {
            if (contribution.getProfiles().contains(uri)) {
                profileContributions.add(contribution.getUri());
            }
        }

        return profileContributions;
    }

    public List<URI> getSortedContributionsInProfile(URI uri) {
        List<Contribution> sortedContributions = new ArrayList<Contribution>();
        Set<Contribution> contributions = metaDataStore.getContributions();
        for (Contribution contribution : contributions) {
            if (contribution.getProfiles().contains(uri)) {
                sortedContributions.add(contribution);
            }
        }
        List<URI> profileContributions = new ArrayList<URI>();
        sortedContributions = dependencyService.orderForUninstall(sortedContributions);
        for (Contribution contribution : sortedContributions) {
            profileContributions.add(contribution.getUri());
        }
        return profileContributions;
    }

    /**
     * Resolves a contribution by its URI.
     *
     * @param contributionUri the contribution URI
     * @return the contribution
     * @throws ContributionNotFoundException if the contribution does not exist
     */
    private Contribution find(URI contributionUri) throws ContributionNotFoundException {
        Contribution contribution = metaDataStore.find(contributionUri);
        if (contribution == null) {
            throw new ContributionNotFoundException("No contribution found for: " + contributionUri);
        }
        return contribution;
    }

    /**
     * Installs a contribution by introspecting and loading it in memory.
     *
     * @param contribution the contribution
     * @throws InstallException if there is an error installing the contribution
     */
    private void install(Contribution contribution) throws InstallException {
        if (ContributionState.STORED != contribution.getState()) {
            throw new ContributionAlreadyInstalledException("Contribution is already installed");
        }
        processManifest(contribution);
        ClassLoader loader = contributionLoader.load(contribution);
        try {
            processContents(contribution, loader);
        } catch (InstallException e) {
            try {
                contributionLoader.unload(contribution);
            } catch (ContributionInUseException ex) {
                // this can't happen
                throw new InstallException(ex);
            }
            throw e;
        }
        contribution.setState(ContributionState.INSTALLED);
    }

    /**
     * Installs a collection of contributions in order of their dependencies.
     *
     * @param contributions the contributions
     * @return the ordered list of contribution URIs
     * @throws InstallException if there is an error installing the contributions
     */
    private List<URI> installInOrder(List<Contribution> contributions) throws InstallException {
        for (Contribution contribution : contributions) {
            // process any SCA manifest information, including imports and exports
            processManifest(contribution);
        }
        // order the contributions based on their dependencies
        try {
            contributions = dependencyService.order(contributions);
        } catch (DependencyException e) {
            throw new InstallException(e);
        }
        for (Contribution contribution : contributions) {
            ClassLoader loader = contributionLoader.load(contribution);
            // continue processing the contributions. As they are ordered, dependencies will resolve correctly
            processContents(contribution, loader);
            contribution.setState(ContributionState.INSTALLED);
            for (ContributionServiceListener listener : listeners) {
                listener.onInstall(contribution);
            }
        }
        List<URI> uris = new ArrayList<URI>(contributions.size());
        for (Contribution contribution : contributions) {
            URI uri = contribution.getUri();
            uris.add(uri);
            String description = contribution.getManifest().getDescription();
            if (description != null) {
                monitor.installed(description);
            }
        }
        return uris;
    }

    /**
     * Processes the contribution manifest.
     *
     * @param contribution the contribution
     * @throws InstallException if there is an error during introspection such as an invalid contribution
     */
    private void processManifest(Contribution contribution) throws InstallException {
        IntrospectionContext context = new DefaultIntrospectionContext();
        processorRegistry.processManifest(contribution, context);
        if (context.hasErrors()) {
            URI uri = contribution.getUri();
            ArtifactValidationFailure failure = new ArtifactValidationFailure(uri, "the contribution manifest (sca-contribution.xml)");
            failure.addFailures(context.getErrors());
            List<ValidationFailure> failures = new ArrayList<ValidationFailure>();
            failures.add(failure);
            ArtifactValidationFailure warning = new ArtifactValidationFailure(uri, "the contribution manifest (sca-contribution.xml)");
            warning.addFailures(context.getWarnings());
            List<ValidationFailure> warnings = new ArrayList<ValidationFailure>();
            warnings.add(warning);
            throw new InvalidContributionException(failures, warnings);
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
     * @throws InstallException if an error occurs during processing
     */
    private void processContents(Contribution contribution, ClassLoader loader) throws InstallException {
        try {
            URI contributionUri = contribution.getUri();
            IntrospectionContext context = new DefaultIntrospectionContext(contributionUri, loader);
            processorRegistry.indexContribution(contribution, context);
            if (context.hasErrors()) {
                throw new InvalidContributionException(context.getErrors(), context.getWarnings());
            }
            metaDataStore.store(contribution);
            context = new DefaultIntrospectionContext(contributionUri, loader);
            processorRegistry.processContribution(contribution, context);
            validateContribution(contribution, context);
            if (context.hasErrors()) {
                throw new InvalidContributionException(context.getErrors(), context.getWarnings());
            } else if (context.hasWarnings()) {
                // there were just warnings, report them
                monitor.contributionWarnings(ValidationUtils.outputWarnings(context.getWarnings()));
            }
            addContributionUri(contribution);
        } catch (StoreException e) {
            throw new InstallException(e);
        }
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
                InvalidDeployable failure = new InvalidDeployable("Deployable composite " + name + " not found in " + uri, uri, name);
                context.addError(failure);
            }

        }
    }

    private void update(URI uri, byte[] checksum, long timestamp) throws UpdateException, IOException, ContributionNotFoundException {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found for: " + uri);
        }
        long archivedTimestamp = contribution.getTimestamp();
        if (timestamp > archivedTimestamp) {
            // TODO update
            for (ContributionServiceListener listener : listeners) {
                listener.onUpdate(contribution);
            }
        } else if (timestamp == archivedTimestamp && Arrays.equals(checksum, contribution.getChecksum())) {
            // TODO update
            for (ContributionServiceListener listener : listeners) {
                listener.onUpdate(contribution);
            }
        }
    }

    /**
     * Stores the contents of a contribution in the archive store if it is not local
     *
     * @param source the contribution source
     * @return the contribution
     * @throws StoreException if an error occurs during the store operation
     */
    private Contribution persist(ContributionSource source) throws StoreException {
        URI contributionUri = source.getUri();
        if (metaDataStore.find(contributionUri) != null) {
            throw new DuplicateContributionException("Contribution is already installed: " + contributionUri);
        }
        URL locationUrl;
        boolean persistent = source.persist();
        if (!persistent) {
            locationUrl = source.getLocation();
        } else {
            InputStream stream = null;
            try {
                stream = source.getSource();
                locationUrl = getRepository().store(contributionUri, stream);
            } catch (IOException e) {
                throw new StoreException(e);
            } catch (RepositoryException e) {
                throw new StoreException(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    monitor.error("Error closing contribution stream", e);
                }
            }

        }
        try {
            String type = source.getContentType();
            if (type == null) {
                type = contentTypeResolver.getContentType(locationUrl);
            }
            byte[] checksum = source.getChecksum();
            long timestamp = source.getTimestamp();
            return new Contribution(contributionUri, locationUrl, checksum, timestamp, type, persistent);
        } catch (ContentTypeResolutionException e) {
            throw new StoreException(e);
        }
    }

    /**
     * Recursively adds the contribution URI to all components.
     *
     * @param contribution the contribution the component is defined in
     */
    private void addContributionUri(Contribution contribution) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                Object value = element.getValue();
                if (value instanceof Composite) {
                    addContributionUri(contribution, (Composite) value);
                }
            }
        }
    }

    /**
     * Adds the contibution URI to a component and its children if it is a composite.
     *
     * @param contribution the contribution
     * @param composite    the composite
     */
    private void addContributionUri(Contribution contribution, Composite composite) {
        for (ComponentDefinition<?> definition : composite.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionUri(contribution, componentType);
            }
            if (definition.getContributionUri() == null) {
                // Check if the contribution URI has already been set. It can be set previously if a composite is used as an implementation
                // (implementation.composite) and is contained in another contribution (i.e. imported into another contribution that uses it). 
                definition.setContributionUri(contribution.getUri());
            }
        }
    }

    private Repository getRepository() {
        if (repository == null) {
            throw new UnsupportedOperationException(Repository.class.getSimpleName() + " not configured");
        }
        return repository;
    }
}
