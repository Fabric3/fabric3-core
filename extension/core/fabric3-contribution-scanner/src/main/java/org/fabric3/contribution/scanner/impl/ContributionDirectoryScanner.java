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
package org.fabric3.contribution.scanner.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.contribution.scanner.spi.FileSystemResource;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactoryRegistry;
import org.fabric3.contribution.scanner.spi.ResourceState;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.ExtensionsInitialized;
import org.fabric3.spi.event.Fabric3Event;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;

/**
 * Periodically scans deployment directories for new, updated, or removed contributions. New contributions are added to the domain and any deployable
 * components activated. Updated components will trigger a redeployment. Removal will perform an undeployment.
 * <p/>
 * The scanner watches deployment directories at a fixed interval. Files are tracked as a {@link FileSystemResource}, which provides a consistent view
 * across various types such as jars and exploded directories. Unknown file types are ignored. At the specified interval, removed files are determined
 * by comparing the current directory contents with the contents from the previous pass. Changes or additions are also determined by comparing the
 * current directory state with that of the previous pass. Detected changes and additions are cached for the following interval. Detected changes and
 * additions from the previous interval are then compared using a timestamp to see if they have changed again. If so, they remain cached. If they have
 * not changed, they are processed, contributed via the ContributionService, and deployed in the domain.
 */
@EagerInit
public class ContributionDirectoryScanner implements Runnable, Fabric3EventListener {
    private ContributionService contributionService;
    private EventService eventService;
    private ScannerMonitor monitor;
    private Domain domain;

    private Set<File> ignored = new HashSet<File>();
    private Map<String, FileSystemResource> cache = new HashMap<String, FileSystemResource>();

    private FileSystemResourceFactoryRegistry registry;
    private List<File> paths;

    private long delay = 2000;
    private ScheduledExecutorService executor;

    public ContributionDirectoryScanner(@Reference FileSystemResourceFactoryRegistry registry,
                                        @Reference ContributionService contributionService,
                                        @Reference(name = "assembly") Domain domain,
                                        @Reference EventService eventService,
                                        @Reference HostInfo hostInfo,
                                        @Monitor ScannerMonitor monitor) {
        this.registry = registry;
        this.contributionService = contributionService;
        this.domain = domain;
        this.eventService = eventService;
        paths = hostInfo.getDeployDirectories();
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @SuppressWarnings({"unchecked"})
    @Init
    public void init() {
        eventService.subscribe(ExtensionsInitialized.class, this);
        // register to be notified when the runtime starts so the scanner thread can be initialized
        eventService.subscribe(RuntimeStart.class, this);
    }

    @Destroy
    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public void onEvent(Fabric3Event event) {
        if (event instanceof ExtensionsInitialized) {
            // process existing files in recovery mode
            List<File> files = new ArrayList<File>();
            for (File path : paths) {
                File[] pathFiles = path.listFiles();
                if (pathFiles != null) {
                    Collections.addAll(files, pathFiles);
                }
            }
            processFiles(files, true);
        } else if (event instanceof RuntimeStart) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(this, 10, delay, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void run() {
        List<File> files = new ArrayList<File>();
        for (File path : paths) {
            if (!path.isDirectory()) {
                // there is no extension directory, return without processing
                continue;
            }
            File[] pathFiles = path.listFiles();
            if (pathFiles != null) {
                Collections.addAll(files, pathFiles);
            }
        }
        processRemovals(files);
        if (files.isEmpty()) {
            // there is no extension directory, return without processing
            return;
        }
        try {
            processFiles(files, false);
            processIgnored();
        } catch (RuntimeException e) {
            monitor.error(e);
        } catch (Error e) {
            monitor.error(e);
            throw e;
        }
    }

    /**
     * Processes the contents of deployment directories and updates the state of cached resources based on those contents.
     *
     * @param files   the files in the deployment directories
     * @param recover true if processing is performed during recovery
     */
    private synchronized void processFiles(List<File> files, boolean recover) {
        boolean wait = false;
        for (File file : files) {
            String name = file.getName();
            FileSystemResource cached = cache.get(name);
            if (cached == null) {
                cached = registry.createResource(file);
                if (cached == null) {
                    // not a known type, ignore
                    if (!name.startsWith(".") && !name.endsWith(".txt") && !ignored.contains(file)) {
                        monitor.ignored(name);
                    }
                    ignored.add(file);
                    continue;
                }
                cache.put(name, cached);
                if (recover) {
                    // recover, do not wait to install
                    cached.setState(ResourceState.ADDED);
                } else {
                    // file may have been ignored previously as it was incomplete such as missing a manifest; remove it from the ignored list
                    ignored.remove(file);
                    wait = true;
                    continue;
                }
            } else {
                if (cached.getState() == ResourceState.ERROR) {
                    FileSystemResource resource = registry.createResource(file);
                    if (cached.getTimestamp() == resource.getTimestamp()) {
                        // corrupt file from a previous run, continue
                        continue;
                    } else {
                        // file has changed since the error was reported, retry
                        cached.setState(ResourceState.DETECTED);
                    }
                } else if (cached.getState() == ResourceState.DETECTED) {
                    if (cached.isChanged()) {
                        wait = true;
                        continue;
                    } else {
                        cached.setState(ResourceState.ADDED);
                    }
                } else if (cached.getState() == ResourceState.PROCESSED) {
                    if (cached.isChanged()) {
                        cached.setState(ResourceState.UPDATED);
                    }
                }
            }
        }
        try {
            if (recover) {
                processAdditions(true);
            } else {
                if (!wait) {
                    processUpdates();
                    processAdditions(false);
                }
            }
        } catch (DeploymentException e) {
            monitor.error(e);
        }
    }

    /**
     * Processes updated resources in the deployment directories.
     *
     * @throws DeploymentException if there is an error during processing
     */
    private synchronized void processUpdates() throws DeploymentException {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        List<FileSystemResource> updatedResources = new ArrayList<FileSystemResource>();
        List<URI> uris = new ArrayList<URI>();
        for (FileSystemResource resource : cache.values()) {
            if (resource.getState() != ResourceState.UPDATED) {
                continue;
            }
            try {
                String name = resource.getName();
                URI artifactUri = new URI(name);
                URL location = resource.getLocation();
                long timestamp = resource.getTimestamp();
                // undeploy any deployed composites in the reverse order that they were deployed in
                List<QName> deployables = contributionService.getDeployedComposites(artifactUri);
                ListIterator<QName> iter = deployables.listIterator(deployables.size());
                while (iter.hasPrevious()) {
                    QName deployable = iter.previous();
                    domain.undeploy(deployable);
                }
                ContributionSource source = new FileContributionSource(artifactUri, location, timestamp, false);
                sources.add(source);
                updatedResources.add(resource);
                uris.add(artifactUri);
            } catch (ContributionException e) {
                resource.setState(ResourceState.ERROR);
                monitor.error(e);
            } catch (URISyntaxException e) {
                resource.setState(ResourceState.ERROR);
                monitor.error(e);
            }
        }
        try {
            contributionService.uninstall(uris);
            contributionService.remove(uris);
            List<URI> contributions = contributionService.contribute(sources);
            domain.include(contributions);
            for (FileSystemResource resource : updatedResources) {
                resource.setState(ResourceState.PROCESSED);
                resource.checkpoint();
            }
        } catch (RemoveException e) {
            throw new DeploymentException(e);
        } catch (ContributionException e) {
            // TODO do something better than this
            monitor.error(e);
        }
    }

    /**
     * Processes added resources in the deployment directories.
     *
     * @param recover true if files are being added in recovery mode
     */
    private synchronized void processAdditions(boolean recover) {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        List<FileSystemResource> addedResources = new ArrayList<FileSystemResource>();
        for (FileSystemResource resource : cache.values()) {
            if (resource.getState() != ResourceState.ADDED) {
                continue;
            }
            String name = resource.getName();
            URL location = resource.getLocation();
            long timestamp = resource.getTimestamp();
            try {
                ContributionSource source = new FileContributionSource(URI.create(name), location, timestamp, false);
                sources.add(source);
                addedResources.add(resource);
            } catch (NoClassDefFoundError e) {
                resource.setState(ResourceState.ERROR);
                monitor.error(e);
            }
        }
        if (!sources.isEmpty()) {
            try {
                // Install contributions, which will be ordered transitively by import dependencies
                List<URI> addedUris = contributionService.contribute(sources);
                // Include the contributions if this is not a recovery operation (recovery will handle inclusion separately)
                if (!recover) {
                    domain.include(addedUris);
                }
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.PROCESSED);
                    resource.checkpoint();
                    monitor.processed(resource.getName());
                }
            } catch (ValidationException e) {
                // print out the validation errors
                monitor.contributionErrors(e.getMessage());
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
            } catch (AssemblyException e) {
                // print out the deployment errors
                monitor.deploymentErrors(e.getMessage());
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
            } catch (ContributionException e) {
                monitor.error(e);
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
            } catch (DeploymentException e) {
                monitor.error(e);
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
            } catch (NoClassDefFoundError e) {
                monitor.error(e);
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
                // don't re-throw the error since the contribution can be safely ignored
            } catch (Error e) {
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
                // re-throw the exception as the runtime may be in an unstable state
                throw e;
            } catch (RuntimeException e) {
                for (FileSystemResource resource : addedResources) {
                    resource.setState(ResourceState.ERROR);
                }
                // re-throw the exception as the runtime may be in an unstable state
                throw e;
            }
        }
    }

    /**
     * process removed files, which results in undeployment.
     *
     * @param files the current contents of the deployment directories
     */
    private synchronized void processRemovals(List<File> files) {
        Map<String, File> index = new HashMap<String, File>(files.size());
        for (File file : files) {
            index.put(file.getName(), file);
        }

        for (Iterator<FileSystemResource> iterator = cache.values().iterator(); iterator.hasNext();) {
            FileSystemResource entry = iterator.next();
            String name = entry.getName();
            URI uri = URI.create(name);
            if (index.get(name) == null) {
                // artifact was removed
                try {
                    iterator.remove();
                    // check that the resource was not deleted by another process
                    if (contributionService.exists(uri)) {
                        // undeploy any deployed composites in the reverse order that they were deployed in
                        List<QName> deployables = contributionService.getDeployedComposites(uri);
                        ListIterator<QName> iter = deployables.listIterator(deployables.size());
                        while (iter.hasPrevious()) {
                            QName deployable = iter.previous();
                            domain.undeploy(deployable);
                        }
                        contributionService.uninstall(uri);
                        contributionService.remove(uri);
                    }
                    monitor.removed(name);
                } catch (ContributionException e) {
                    monitor.removalError(name, e);
                } catch (DeploymentException e) {
                    monitor.removalError(name, e);
                }
            }
        }
    }

    private synchronized void processIgnored() {
        for (Iterator<File> iter = ignored.iterator(); iter.hasNext();) {
            File file = iter.next();
            if (!file.exists()) {
                iter.remove();
            }
        }
    }

}
