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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.contribution.scanner.spi.FileSystemResource;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactoryRegistry;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.VoidService;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.ExtensionsInitialized;
import org.fabric3.spi.event.Fabric3Event;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;

/**
 * Periodically scans a directory for new, updated, or removed contributions. New contributions are added to the domain and any deployable components
 * activated. Updated components will trigger re-activation of previously deployed components. Removal will remove the contribution from the domain
 * and de-activate any associated deployed components.
 * <p/>
 * The scanner watches the deployment directory at a fixed interval. Files are tracked as a {@link FileSystemResource}, which provides a consistent
 * metadata view across various types such as jars and exploded directories. Unknown file types are ignored. At the specified interval, removed files
 * are determined by comparing the current directory contents with the contents from the previous pass. Changes or additions are also determined by
 * comparing the current directory state with that of the previous pass. Detected changes and additions are cached for the following interval.
 * Detected changes and additions from the previous interval are then compared using a checksum to see if they have changed again. If so, they remain
 * cached. If they have not changed, they are processed, contributed via the ContributionService, and deployed in the domain.
 */
@Service(VoidService.class)
@EagerInit
public class ContributionDirectoryScanner implements Runnable, Fabric3EventListener {
    private final Map<String, FileSystemResource> cache = new HashMap<String, FileSystemResource>();
    private final Map<String, FileSystemResource> errorCache = new HashMap<String, FileSystemResource>();
    private final ContributionService contributionService;
    private final EventService eventService;
    private final ScannerMonitor monitor;
    private final Domain domain;
    private Map<String, URI> processed = new HashMap<String, URI>();
    private Set<File> ignored = new HashSet<File>();

    private FileSystemResourceFactoryRegistry registry;
    private File path;

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
        path = hostInfo.getDeployDirectory();
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setPath(String dir) {
        this.path = new File(dir);
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
        executor.shutdownNow();
    }

    public void onEvent(Fabric3Event event) {
        if (event instanceof ExtensionsInitialized) {
            // process existing files in recovery mode
            File[] files = path.listFiles();
            recover(files);
        } else if (event instanceof RuntimeStart) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(this, 10, delay, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void run() {
        if (!path.isDirectory()) {
            // there is no extension directory, return without processing
            return;
        }
        try {
            File[] files = path.listFiles();
            processRemovals(files);
            processFiles(files);
            processIgnored();
        } catch (RuntimeException e) {
            monitor.error(e);
        } catch (Error e) {
            monitor.error(e);
            throw e;
        }
    }

    private synchronized void recover(File[] files) {
        try {
            List<File> contributions = new ArrayList<File>();
            for (File file : files) {
                String name = file.getName();
                FileSystemResource resource = null;
                FileSystemResource cached = errorCache.get(name);
                if (cached != null) {
                    resource = registry.createResource(file);
                    assert resource != null;
                    resource.reset();
                    if (Arrays.equals(cached.getChecksum(), resource.getChecksum())) {
                        // corrupt file from a previous run, continue
                        continue;
                    } else {
                        // file has changed since the error was reported, retry
                        errorCache.remove(name);
                    }
                }
                cached = cache.get(name);
                if (cached == null) {
                    // the file has been added
                    if (resource == null) {
                        resource = registry.createResource(file);
                    }
                    if (resource == null) {
                        // not a known type, ignore
                        continue;
                    }
                    resource.reset();
                    // cache the resource and wait to the next run to see if it has changed
                    cache.put(name, resource);
                    contributions.add(file);
                }
            }
            processAdditions(contributions, true);
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    private synchronized void processFiles(File[] files) {
        boolean wait = false;
        for (File file : files) {
            try {
                String name = file.getName();
                FileSystemResource resource = null;
                FileSystemResource cached = errorCache.get(name);
                if (cached != null) {
                    resource = registry.createResource(file);
                    assert resource != null;
                    resource.reset();
                    if (Arrays.equals(cached.getChecksum(), resource.getChecksum())) {
                        // corrupt file from a previous run, continue
                        continue;
                    } else {
                        // file has changed since the error was reported, retry
                        errorCache.remove(name);
                    }
                }
                cached = cache.get(name);
                if (cached == null) {
                    // the file has been added
                    if (resource == null) {
                        resource = registry.createResource(file);
                    }
                    if (resource == null) {
                        // not a known type, ignore
                        if (!name.startsWith(".") && !name.endsWith(".txt") && !ignored.contains(file)) {
                            monitor.ignored(name);
                        }
                        ignored.add(file);
                        continue;
                    }
                    resource.reset();
                    // cache the resource and wait to the next run to see if it has changed
                    cache.put(name, resource);
                    wait = true;
                } else {
                    // already cached from a previous run
                    if (cached.isChanged()) {
                        // contents are still being updated, wait until next run
                        wait = true;
                    }
                }
            } catch (IOException e) {
                monitor.error(e);
            }
        }
        if (!wait) {
            sortAndProcessChanges(files);
        }
    }

    private void sortAndProcessChanges(File[] files) {
        try {
            List<File> updates = new ArrayList<File>();
            List<File> additions = new ArrayList<File>();
            for (File file : files) {
                // check if it is in the store
                String name = file.getName();
                boolean isProcessed = processed.containsKey(name);
                boolean isError = errorCache.containsKey(name);
                if (!isError && isProcessed && !ignored.contains(file)) {
                    // updated
                    updates.add(file);
                } else if (!isError && !isProcessed && !ignored.contains(file)) {
                    // an addition
                    additions.add(file);
                }
            }
            processUpdates(updates);
            processAdditions(additions, false);
        } catch (IOException e) {
            monitor.error(e);
        } catch (DeploymentException e) {
            monitor.error(e);
        }
    }

    private synchronized void processUpdates(List<File> files) throws IOException, DeploymentException {
        for (File file : files) {
            String name = file.getName();
            URI artifactUri = processed.get(name);
            URL location = file.toURI().normalize().toURL();
            FileSystemResource cached = cache.remove(name);
            long timestamp = file.lastModified();
            long previousTimestamp = contributionService.getContributionTimestamp(artifactUri);
            if (timestamp > previousTimestamp) {
                try {
                    ContributionSource source = new FileContributionSource(artifactUri, location, timestamp, false);
                    // undeploy any deployed composites in the reverse order that they were deployed in
                    List<QName> deployables = contributionService.getDeployedComposites(artifactUri);
                    ListIterator<QName> iter = deployables.listIterator(deployables.size());
                    while (iter.hasPrevious()) {
                        QName deployable = iter.previous();
                        domain.undeploy(deployable);
                    }
                    contributionService.remove(artifactUri);
                    contributionService.contribute(source);
                } catch (ContributionException e) {
                    errorCache.put(name, cached);
                    monitor.error(e);
                }
                monitor.updated(artifactUri.toString());
            }
            // TODO undeploy and redeploy
        }
    }

    private synchronized void processAdditions(List<File> files, boolean recover) throws IOException {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        List<FileSystemResource> addedResources = new ArrayList<FileSystemResource>();
        for (File file : files) {
            String name = file.getName();
            FileSystemResource cached = cache.remove(name);
            addedResources.add(cached);
            URL location = file.toURI().normalize().toURL();
            long timestamp = file.lastModified();
            try {
                ContributionSource source = new FileContributionSource(URI.create(name), location, timestamp, false);
                sources.add(source);
            } catch (NoClassDefFoundError e) {
                errorCache.put(name, cached);
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
                for (URI uri : addedUris) {
                    String name = uri.toString();
                    // URI is the file name
                    processed.put(name, uri);
                    monitor.processed(name);
                }
            } catch (ValidationException e) {
                // print out the validation errors
                monitor.contributionErrors(e.getMessage());
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
            } catch (AssemblyException e) {
                // print out the deployment errors
                monitor.deploymentErrors(e.getMessage());
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
            } catch (ContributionException e) {
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
            } catch (DeploymentException e) {
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
                monitor.error(e);
            } catch (Error e) {
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
                throw e;
            } catch (RuntimeException e) {
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
                throw e;
            }
        }
    }

    private synchronized void processRemovals(File[] files) {
        Map<String, File> index = new HashMap<String, File>(files.length);
        for (File file : files) {
            index.put(file.getName(), file);
        }

        List<String> removed = new ArrayList<String>();
        for (Map.Entry<String, URI> entry : processed.entrySet()) {
            String filename = entry.getKey();
            URI uri = entry.getValue();
            if (index.get(filename) == null) {
                // artifact was removed
                try {
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
                    removed.add(filename);
                    monitor.removed(filename);
                } catch (ContributionException e) {
                    monitor.removalError(filename, e);
                } catch (DeploymentException e) {
                    monitor.removalError(filename, e);
                }
            }
        }
        for (String removedName : removed) {
            processed.remove(removedName);
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
