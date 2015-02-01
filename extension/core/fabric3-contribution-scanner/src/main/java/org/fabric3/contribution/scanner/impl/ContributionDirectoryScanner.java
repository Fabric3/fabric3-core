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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;
import org.fabric3.api.host.contribution.ValidationException;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.contribution.scanner.spi.FileSystemResource;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactoryRegistry;
import org.fabric3.contribution.scanner.spi.FileSystemResourceState;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.ExtensionsInitialized;
import org.fabric3.spi.runtime.event.Fabric3Event;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStart;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Scans deployment directories for contributions. In production mode, deployment directories will be scanned once at startup and any contained contributions
 * will be deployed. In the default dynamic (non-production) mode, scanning will be periodic with support for adding, updating, and removing contributions. <p/>
 * In dynamic mode, the scanner watches deployment directories at a fixed interval. Files are tracked as a {@link FileSystemResource}, which provides a
 * consistent view across various types such as jars and exploded directories. Unknown file types are ignored. At the specified interval, removed files are
 * determined by comparing the current directory contents with the contents from the previous pass. Changes or additions are also determined by comparing the
 * current directory state with that of the previous pass. Detected changes and additions are cached for the following interval. Detected changes and additions
 * from the previous interval are then compared using a timestamp to see if they have changed again. If so, they remain cached. If they have not changed, they
 * are processed, contributed via the ContributionService, and deployed in the domain.
 */
@EagerInit
public class ContributionDirectoryScanner implements Runnable, Fabric3EventListener {
    private ContributionService contributionService;
    private FileSystemResourceFactoryRegistry registry;
    private EventService eventService;
    private ScannerMonitor monitor;
    private Domain domain;
    private List<File> paths;
    private long delay = 2000;
    private boolean production = false;

    private ScheduledExecutorService executor;
    private Set<File> ignored = new HashSet<>();
    private Map<String, FileSystemResource> cache = new HashMap<>();
    List<URI> notSeen = new ArrayList<>(); // contributions added when the runtime was offline and hence not previously seen by the scanner
    private Set<String> tracked = new HashSet<>();

    public ContributionDirectoryScanner(@Reference ContributionService contributionService,
                                        @Reference(name = "assembly") Domain domain,
                                        @Reference FileSystemResourceFactoryRegistry registry,
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
    public void setProduction(boolean production) {
        this.production = production;
    }

    @Property(required = false)
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @SuppressWarnings({"unchecked"})
    @Init
    public void init() {
        eventService.subscribe(ExtensionsInitialized.class, this);
        // Register to be notified when the runtime starts to perform the following:
        //  1. Contributions installed to deployment directories when the runtime is offline are deployed to the domain
        //  2. The scanner thread is initialized
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
            if (paths == null) {
                return;
            }
            // process existing files in recovery mode
            List<File> files = new ArrayList<>();
            for (File path : paths) {
                File[] pathFiles = path.listFiles();
                if (pathFiles != null) {
                    Collections.addAll(files, pathFiles);
                }
            }
            processFiles(files, true);
        } else if (event instanceof RuntimeStart) {
            try {
                domain.include(notSeen);
            } catch (Fabric3Exception e) {
                monitor.error(e);
            }
            notSeen.clear();
            if (!production) {
                executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleWithFixedDelay(this, 10, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    public synchronized void run() {
        if (paths == null) {
            return;
        }
        List<File> files = new ArrayList<>();
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
            // there are no files to process
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
                    cached.setState(FileSystemResourceState.ADDED);
                } else {
                    // file may have been ignored previously as it was incomplete such as missing a manifest; remove it from the ignored list
                    ignored.remove(file);
                    continue;
                }
            } else {
                if (cached.getState() == FileSystemResourceState.ERROR) {
                    if (cached.isChanged()) {
                        // file has changed since the error was reported, set to detected
                        cached.setState(FileSystemResourceState.DETECTED);
                        cached.checkpoint();
                    } else {
                        // corrupt file from a previous run, continue
                        continue;
                    }
                } else if (cached.getState() == FileSystemResourceState.DETECTED) {
                    if (cached.isChanged()) {
                        // updates may still be pending, wait until the next pass
                        continue;
                    } else {
                        cached.setState(FileSystemResourceState.ADDED);
                        cached.checkpoint();
                    }
                } else if (cached.getState() == FileSystemResourceState.PROCESSED) {
                    if (cached.isChanged()) {
                        cached.setState(FileSystemResourceState.UPDATED);
                        cached.checkpoint();
                    }
                }
            }
        }
        if (recover) {
            processAdditions(true);
        } else {
            processUpdates();
            processAdditions(false);
        }
    }

    /**
     * Processes updated resources in the deployment directories.
     */
    private synchronized void processUpdates() {
        List<ContributionSource> sources = new ArrayList<>();
        List<FileSystemResource> updatedResources = new ArrayList<>();
        List<URI> uris = new ArrayList<>();
        for (FileSystemResource resource : cache.values()) {
            if (resource.getState() != FileSystemResourceState.UPDATED) {
                continue;
            }
            try {
                String name = resource.getName();
                URI artifactUri = new URI(name);
                URL location = resource.getLocation();
                long timestamp = resource.getTimestamp();
                // undeploy any deployed composites in the reverse order that they were deployed in
                try {
                    domain.undeploy(artifactUri, false);
                } catch (Fabric3Exception e) {
                    monitor.error(e);
                    return;
                }
                // if the resource has changed, wait until the next pass as updates may still be in progress
                if (resource.isChanged()) {
                    resource.checkpoint();
                    continue;
                }
                ContributionSource source = new FileContributionSource(artifactUri, location, timestamp, false);
                sources.add(source);
                updatedResources.add(resource);
                uris.add(artifactUri);
            } catch (URISyntaxException e) {
                resource.setState(FileSystemResourceState.ERROR);
                monitor.error(e);
            }
        }
        try {
            if (!uris.isEmpty()) {
                contributionService.uninstall(uris);
                contributionService.remove(uris);
            }
            if (!sources.isEmpty()) {
                List<URI> stored = contributionService.store(sources);
                List<URI> contributions = contributionService.install(stored);
                domain.include(contributions);
                for (FileSystemResource resource : updatedResources) {
                    resource.setState(FileSystemResourceState.PROCESSED);
                    resource.checkpoint();
                    monitor.processed(resource.getName());
                }
            }
        } catch (Fabric3Exception e) {
            for (FileSystemResource resource : updatedResources) {
                resource.setState(FileSystemResourceState.ERROR);
            }
            monitor.error(e);
        }
    }

    /**
     * Processes added resources in the deployment directories.
     *
     * @param recover true if files are being added in recovery mode
     */
    private synchronized void processAdditions(boolean recover) {
        List<ContributionSource> sources = new ArrayList<>();
        List<FileSystemResource> addedResources = new ArrayList<>();

        for (FileSystemResource resource : cache.values()) {
            if (resource.getState() != FileSystemResourceState.ADDED || resource.isChanged()) {
                resource.checkpoint();
                continue;
            }
            String name = resource.getName();

            URL location = resource.getLocation();
            long timestamp = resource.getTimestamp();
            URI uri = URI.create(name);
            ContributionSource source = new FileContributionSource(uri, location, timestamp, false);
            sources.add(source);
            addedResources.add(resource);

            boolean seen = tracked.contains(name);
            if (!seen && recover) {
                // the contribution was not seen previously, schedule it to be deployed when the domain recovers
                notSeen.add(uri);
            }

            // track the addition
            tracked.add(name);

        }
        if (!sources.isEmpty()) {
            try {
                // Install contributions, which will be ordered transitively by import dependencies
                List<URI> stored = contributionService.store(sources);
                List<URI> addedUris = contributionService.install(stored);
                // Include the contributions if this is not a recovery operation (recovery will handle inclusion separately)
                if (!recover) {
                    domain.include(addedUris);
                }
                for (FileSystemResource resource : addedResources) {
                    resource.setState(FileSystemResourceState.PROCESSED);
                    resource.checkpoint();
                    monitor.processed(resource.getName());
                }
            } catch (ValidationException e) {
                // remove from not seen: FABRICTHREE-583
                for (ContributionSource source : sources) {
                    notSeen.remove(source.getUri());
                }
                // print out the validation errors
                monitor.contributionErrors(e.getMessage());
                for (FileSystemResource resource : addedResources) {
                    resource.setState(FileSystemResourceState.ERROR);
                }
            } catch (AssemblyException e) {
                // print out the deployment errors
                monitor.deploymentErrors(e.getMessage());
                for (FileSystemResource resource : addedResources) {
                    resource.setState(FileSystemResourceState.ERROR);
                }
            } catch (NoClassDefFoundError | Fabric3Exception e) {
                handleError(e, addedResources);
            } catch (Error | RuntimeException e) {
                for (FileSystemResource resource : addedResources) {
                    resource.setState(FileSystemResourceState.ERROR);
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
        Map<String, File> index = new HashMap<>(files.size());
        for (File file : files) {
            index.put(file.getName(), file);
        }

        for (Iterator<FileSystemResource> iterator = cache.values().iterator(); iterator.hasNext(); ) {
            FileSystemResource entry = iterator.next();
            String name = entry.getName();

            URI uri = URI.create(name);
            if (index.get(name) == null) {
                // artifact was removed
                try {
                    // track the removal
                    tracked.remove(name);
                    iterator.remove();
                    // check that the resource was not deleted by another process
                    if (contributionService.exists(uri)) {
                        domain.undeploy(uri, false);
                        contributionService.uninstall(uri);
                        contributionService.remove(uri);
                    }
                    monitor.removed(name);
                } catch (Fabric3Exception e) {
                    monitor.removalError(name, e);
                }
            }
        }
    }

    private synchronized void processIgnored() {
        for (Iterator<File> iter = ignored.iterator(); iter.hasNext(); ) {
            File file = iter.next();
            if (!file.exists()) {
                iter.remove();
            }
        }
    }

    private void handleError(Throwable e, List<FileSystemResource> addedResources) {
        monitor.error(e);
        for (FileSystemResource resource : addedResources) {
            resource.setState(FileSystemResourceState.ERROR);
        }
    }

}
