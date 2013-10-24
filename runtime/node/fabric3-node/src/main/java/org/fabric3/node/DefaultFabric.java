/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.node;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.fabric3.api.host.RuntimeMode;
import org.fabric3.api.host.classloader.MaskingClassLoader;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;
import org.fabric3.api.host.contribution.UrlContributionSource;
import org.fabric3.api.host.monitor.DelegatingDestinationRouter;
import org.fabric3.api.host.os.OperatingSystem;
import org.fabric3.api.host.runtime.BootConfiguration;
import org.fabric3.api.host.runtime.BootstrapFactory;
import org.fabric3.api.host.runtime.BootstrapHelper;
import org.fabric3.api.host.runtime.BootstrapService;
import org.fabric3.api.host.runtime.DefaultHostInfo;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.HiddenPackages;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.RuntimeConfiguration;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.runtime.ScanException;
import org.fabric3.api.host.runtime.ShutdownException;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.node.Domain;
import org.fabric3.api.node.Fabric;
import org.fabric3.api.node.FabricException;
import org.w3c.dom.Document;

/**
 * The default implementation of a fabric connection.
 */
public class DefaultFabric implements Fabric {
    private static final File SYNTHETIC_DIRECTORY = new File("notfound");
    private static final String SYSTEM_COMPOSITE = "META-INF/system.composite";
    private static final URI DOMAIN_URI = URI.create("fabric3://runtime/NodeDomain");
    public static final String ASM_PACKAGE = "org.objectweb.asm.";
    private URL configUrl;

    private enum State {
        UNINITIALIZED, RUNNING
    }

    private File tempDirectory;
    private File extensionsDirectory;
    private File dataDirectory;

    private State state = State.UNINITIALIZED;

    private Fabric3Runtime runtime;
    private RuntimeCoordinator coordinator;

    private Domain domain;

    private Set<String> extensions = new HashSet<String>();
    private Set<URL> profileLocations = new HashSet<URL>();
    private Set<String> profiles = new HashSet<String>();
    private Set<URL> extensionLocations = new HashSet<URL>();

    /**
     * Constructor.
     *
     * @param configUrl the system configuration. If null, this implementation attempts to resolve and use the default configuration.
     */
    public DefaultFabric(URL configUrl) {
        this.configUrl = configUrl;
        String id = UUID.randomUUID().toString();
        tempDirectory = new File(System.getProperty("java.io.tmpdir"), ".f3-" + id);
        extensionsDirectory = new File(tempDirectory, "extensions");
        dataDirectory = new File(tempDirectory, "data");
        createDirectories();
    }

    public Fabric addProfile(String name) {
        profiles.add(name);
        return this;
    }

    public Fabric addProfile(URL location) {
        profileLocations.add(location);
        return this;
    }

    public Fabric addExtension(String name) {
        extensions.add(name);
        return this;
    }

    public Fabric addExtension(URL location) {
        extensionLocations.add(location);
        return this;
    }

    public Fabric start() throws FabricException {
        if (state != State.UNINITIALIZED) {
            throw new IllegalStateException("In wrong state: " + state);
        }
        DelegatingDestinationRouter router = new DelegatingDestinationRouter();
        try {

            ClassLoader fabricClassLoader = getClass().getClassLoader();
            ClassLoader rootClassLoader = fabricClassLoader.getParent();

            MaskingClassLoader maskingClassLoader = new MaskingClassLoader(rootClassLoader, HiddenPackages.getPackages());

            // change the fabric classloader parent to the masking classloader so overridden classes can be masked
            ClassLoaderUtils.changeParentClassLoader(fabricClassLoader, maskingClassLoader);

            // create the classloaders for booting the runtime
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(fabricClassLoader, SYNTHETIC_DIRECTORY);
            MaskingClassLoader maskingHostLoader = new MaskingClassLoader(hostLoader, ASM_PACKAGE);

            // the boot loader needs to have unmasked access to the host loader
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, SYNTHETIC_DIRECTORY);
            MaskingClassLoader maskingBootLoader = new MaskingClassLoader(bootLoader, ASM_PACKAGE);

            BootstrapService bootstrapService = BootstrapFactory.getService(maskingBootLoader);

            // load the system configuration
            UrlSource urlSource = resolveSystemConfiguration(configUrl);
            Document systemConfig = bootstrapService.loadSystemConfig(urlSource);

            URI domainName = bootstrapService.parseDomainName(systemConfig);

            RuntimeMode mode = bootstrapService.parseRuntimeMode(systemConfig);

            String environment = bootstrapService.parseEnvironment(systemConfig);

            String zoneName = bootstrapService.parseZoneName(systemConfig, mode);

            String defaultRuntimeName = UUID.randomUUID().toString();
            String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, defaultRuntimeName, mode);

            // create the HostInfo and runtime
            HostInfo hostInfo = createHostInfo(runtimeName, zoneName, mode, domainName, environment);

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, null, router);

            runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            boolean onlyCore = profiles.isEmpty() && profileLocations.isEmpty() && extensionLocations.isEmpty() && extensions.isEmpty();
            List<ContributionSource> extensionSources = scanExtensions(onlyCore);
            if (!onlyCore) {
                addConfiguredExtensions(extensionSources);
            }

            BootConfiguration configuration = new BootConfiguration();
            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(maskingHostLoader);
            configuration.setBootClassLoader(maskingBootLoader);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(extensionSources);

            // boot the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.boot();

            coordinator.load();
            coordinator.joinDomain();

            state = State.RUNNING;
            return this;
        } catch (RuntimeException e) {
            router.flush(System.out);
            throw new FabricException(e);
        } catch (Exception e) {
            router.flush(System.out);
            throw new FabricException(e);
        }
    }

    public Fabric stop() throws FabricException {
        if (state != State.RUNNING) {
            throw new IllegalStateException("Not in running state: " + state);
        }
        try {
            coordinator.shutdown();
            state = State.UNINITIALIZED;
            if (tempDirectory.exists()) {
                FileHelper.cleanDirectory(tempDirectory);
            }
            return this;
        } catch (ShutdownException e) {
            throw new FabricException(e);
        } catch (IOException e) {
            throw new FabricException(e);
        }
    }

    public <T> Fabric registerSystemService(Class<T> interfaze, T instance) throws FabricException {
        throw new UnsupportedOperationException();
    }

    public Domain getDomain() {
        if (state != State.RUNNING) {
            throw new IllegalStateException("Not in started state: " + state);
        }
        if (domain == null) {
            domain = runtime.getComponent(Domain.class, DOMAIN_URI);
        }
        return domain;
    }

    /**
     * Adds configured profiles and extensions to the sources.
     *
     * @param sources the sources
     * @throws IOException if there is an error adding to the sources
     */
    private void addConfiguredExtensions(List<ContributionSource> sources) throws IOException {
        File repositoryDirectory = ArchiveUtils.getJarDirectory(DefaultFabric.class).getParentFile().getParentFile();
        for (String profile : profiles) {
            File profileArchive = ArchiveUtils.getProfileArchive(profile, repositoryDirectory);
            List<File> expanded = ArchiveUtils.unpack(profileArchive, extensionsDirectory);
            addSources(expanded, sources);
        }

        for (String extension : extensions) {
            File extensionArchive = ArchiveUtils.getExtensionArchive(extension, repositoryDirectory);
            URI uri = URI.create(extensionArchive.getName());
            URL location = extensionArchive.toURI().toURL();
            ContributionSource source = new FileContributionSource(uri, location, -1, true);
            sources.add(source);
        }

        addContributionSources(profileLocations, sources);
        addContributionSources(extensionLocations, sources);
    }

    /**
     * Adds the archive urls to the sources.
     *
     * @param sources the sources
     */
    private void addContributionSources(Set<URL> locations, List<ContributionSource> sources) {
        for (URL location : locations) {
            try {
                UrlContributionSource source = new UrlContributionSource(location.toURI(), location, false);
                sources.add(source);
            } catch (URISyntaxException e) {
                throw new FabricException(e);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectories() throws FabricException {
        // clear out the tmp directory
        if (tempDirectory.exists()) {
            try {
                FileHelper.cleanDirectory(tempDirectory);
            } catch (IOException e) {
                throw new FabricException(e);
            }
        }
        tempDirectory.mkdirs();
        extensionsDirectory.mkdirs();
        dataDirectory.mkdirs();
    }

    private UrlSource resolveSystemConfiguration(URL configUrl) {
        if (configUrl == null) {
            configUrl = getClass().getClassLoader().getResource("META-INF/f3.default.config.xml");
            if (configUrl == null) {
                throw new FabricException("Default system configuration not found");
            }
        }
        return new UrlSource(configUrl);
    }

    private File getRuntimeDirectory(RuntimeMode mode) throws IOException {
        //  calculate config directories based on the mode the runtime is booted in
        File jarDirectory = ArchiveUtils.getJarDirectory(DefaultFabric.class);
        File root = new File(jarDirectory, "runtimes");
        return new File(root, mode.toString().toLowerCase());

    }

    private HostInfo createHostInfo(String runtimeName, String zoneName, RuntimeMode mode, URI domainName, String environment) throws IOException {

        File runtimeDirectory = getRuntimeDirectory(mode);

        List<File> deployDirs = Collections.emptyList();

        OperatingSystem os = BootstrapHelper.getOperatingSystem();

        return new DefaultHostInfo(runtimeName,
                                   zoneName,
                                   mode,
                                   environment,
                                   domainName,
                                   runtimeDirectory,
                                   SYNTHETIC_DIRECTORY,
                                   SYNTHETIC_DIRECTORY,
                                   runtimeDirectory,
                                   dataDirectory,
                                   tempDirectory,
                                   deployDirs,
                                   os,
                                   false);
    }

    /**
     * Scans for extensions on a classpath.
     *
     * @param onlyCore if true, only scan for core extensions; ignore all others as they will be explicitly configured
     * @return the sources
     * @throws ScanException if there is a scan error
     */
    private List<ContributionSource> scanExtensions(final boolean onlyCore) throws ScanException {
        try {

            List<File> archives = scanClasspathForProfileArchives();

            if (archives.size() == 0) {
                throw new ScanException("Core extension archive not found");
            }

            List<ContributionSource> sources = new ArrayList<ContributionSource>();
            List<File> extensionsFiles = new ArrayList<File>();

            for (File extension : archives) {
                // if profiles and/or extensions are explicitly configured, only load the core Fabric extensions and ignore all other extensions/profiles on
                // the classpath
                if (onlyCore && !extension.getName().contains("fabric3-node-extensions")) {
                    continue;
                }
                extensionsFiles.addAll(ArchiveUtils.unpack(extension, extensionsDirectory));
            }

            addSources(extensionsFiles, sources);
            return sources;
        } catch (IOException e) {
            throw new ScanException("Error scanning extensions", e);
        }
    }

    /**
     * Adds contribution sources based on the list of files to the given sources.
     *
     * @param files   the files
     * @param sources the sources to add to
     * @throws MalformedURLException if there is an error reading a file name
     */
    private void addSources(List<File> files, List<ContributionSource> sources) throws MalformedURLException {
        for (File file : files) {
            URL location = file.toURI().toURL();
            ContributionSource source;
            if (file.isDirectory()) {
                continue;

            } else {
                URI uri = URI.create(file.getName());
                source = new FileContributionSource(uri, location, -1, true);
            }
            sources.add(source);
        }
    }

    /**
     * Returns the installed profiles by scanning the classpath for F3 manifest entries.
     *
     * @return a collection containing the archive files
     * @throws IOException if there is an error scanning for extensions
     */
    private List<File> scanClasspathForProfileArchives() throws IOException {
        Enumeration<URL> manifests = getClass().getClassLoader().getResources("extensions/F3-MANIFEST.MF");
        List<File> extensionFiles = new ArrayList<File>();
        while (manifests.hasMoreElements()) {
            // determine the containing archive name by removing the jar:file: protocol prefix and the manifest suffix
            URL manifest = manifests.nextElement();
            String path = manifest.getPath();
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }
            path = path.substring(0, path.length() - 27);      //27  = "!/extensions/F3-MANIFEST.MF"
            extensionFiles.add(new File(path));
        }
        return extensionFiles;
    }

}

