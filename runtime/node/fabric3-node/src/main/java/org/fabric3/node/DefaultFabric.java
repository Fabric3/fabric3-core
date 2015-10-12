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
 */
package org.fabric3.node;

import javax.servlet.Servlet;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
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
import org.fabric3.api.host.runtime.ComponentRegistration;
import org.fabric3.api.host.runtime.DefaultHostInfoBuilder;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.HiddenPackages;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.RuntimeConfiguration;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.node.Domain;
import org.fabric3.api.node.Fabric;
import org.fabric3.api.node.FabricException;
import org.fabric3.spi.host.ServletHost;
import org.w3c.dom.Document;

/**
 * The default implementation of a fabric connection.
 */
public class DefaultFabric implements Fabric {
    private static final File SYNTHETIC_DIRECTORY = new File("notfound");
    public static final String ASM_PACKAGE = "org.objectweb.asm.";
    private URL configUrl;

    private enum State {
        UNINITIALIZED, STARTED, RUNNING
    }

    private File tempDirectory;
    private File extensionsDirectory;
    private File dataDirectory;

    private State state = State.UNINITIALIZED;

    private Fabric3Runtime runtime;
    private RuntimeCoordinator coordinator;

    private Domain domain;

    private Set<String> extensions = new HashSet<>();
    private Set<URL> profileLocations = new HashSet<>();
    private Set<String> profiles = new HashSet<>();
    private Set<URL> extensionLocations = new HashSet<>();
    private List<ComponentRegistration> registrations = new ArrayList<>();

    private FabricServletHost host;

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
        startRuntime();
        startTransports();
        return this;
    }

    public Fabric startRuntime() {
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
            ScannedExtensions scannedExtensions = scanExtensions(onlyCore);
            if (!onlyCore) {
                addConfiguredExtensions(scannedExtensions);
            }

            BootConfiguration configuration = new BootConfiguration();
            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(maskingHostLoader);
            configuration.setBootClassLoader(maskingBootLoader);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(scannedExtensions.sources);
            configuration.addRegistrations(registrations);

            // boot the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.boot();

            coordinator.load();
            coordinator.startRuntime();

            state = State.STARTED;
            return this;
        } catch (Exception e) {
            router.flush(System.out);
            throw new FabricException(e);
        }
    }

    public Fabric startTransports() {
        if (state != State.STARTED) {
            throw new IllegalStateException("In wrong state: " + state);
        }
        coordinator.startTransports();
        state = State.RUNNING;
        return this;
    }

    public Fabric stop() throws FabricException {
        if (state != State.RUNNING) {
            throw new IllegalStateException("Not in running state: " + state);
        }
        coordinator.shutdown();
        state = State.UNINITIALIZED;
        if (tempDirectory.exists()) {
            try {
                FileHelper.cleanDirectory(tempDirectory);
            } catch (Fabric3Exception e) {
                if (tempDirectory.exists()) {
                    FileHelper.forceDeleteOnExit(tempDirectory);
                }
            }
        }
        return this;
    }

    public <T> T createTransportDispatcher(Class<T> interfaze, Map<String, Object> properties) {
        if (Servlet.class.isAssignableFrom(interfaze)) {
            if (host == null) {
                int httpPort = (Integer) properties.getOrDefault("http.port", 8080);
                int httpsPort = (Integer) properties.getOrDefault("https.port", 4242);
                URL httpUrl = (URL) properties.get("http.url");
                URL httpsUrl = (URL) properties.get("https.url");
                String contextPath = (String) properties.getOrDefault("http.context", "fabric3");
                host = new FabricServletHost(httpPort, httpsPort, httpUrl, httpsUrl, contextPath);
                registerSystemService(ServletHost.class, host);
            }
            return interfaze.cast(host);
        }
        return null;
    }

    public <T> Fabric registerSystemService(Class<T> interfaze, T instance) throws FabricException {
        ComponentRegistration registration = new ComponentRegistration(interfaze.getSimpleName(), interfaze, instance, false);
        registrations.add(registration);
        return this;
    }

    public Domain getDomain() {
        if (state != State.RUNNING) {
            throw new IllegalStateException("Not in started state: " + state);
        }
        if (domain == null) {
            domain = runtime.getComponent(Domain.class, Names.NODE_DOMAIN_URI);
        }
        return domain;
    }

    /**
     * Adds configured profiles and extensions to the sources.
     *
     * @param scannedExtensions the scanned extensions
     * @throws IOException if there is an error adding to the sources
     */
    private void addConfiguredExtensions(ScannedExtensions scannedExtensions) throws IOException {
        File repositoryDirectory = ArchiveUtils.getJarDirectory(DefaultFabric.class).getParentFile().getParentFile();
        for (String profile : profiles) {
            boolean found = false;
            String name = profile.startsWith("profile-") ? profile : "profile-" + profile;
            for (File archive : scannedExtensions.profileArchives) {
                if (archive.getName().startsWith(name)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            File profileArchive = ArchiveUtils.getProfileArchive(profile, repositoryDirectory);
            List<File> expanded = ArchiveUtils.unpack(profileArchive, extensionsDirectory);
            addSources(expanded, scannedExtensions.sources);
        }

        for (String extension : extensions) {
            File extensionArchive = ArchiveUtils.getExtensionArchive(extension, repositoryDirectory);
            URI uri = URI.create(extensionArchive.getName());
            URL location = extensionArchive.toURI().toURL();
            ContributionSource source = new FileContributionSource(uri, location, -1, true);
            scannedExtensions.sources.add(source);
        }

        addContributionSources(profileLocations, scannedExtensions.sources);
        addContributionSources(extensionLocations, scannedExtensions.sources);
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
            FileHelper.cleanDirectory(tempDirectory);
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

    private File getRuntimeDirectory(RuntimeMode mode) {
        //  calculate config directories based on the mode the runtime is booted in
        File jarDirectory = ArchiveUtils.getJarDirectory(DefaultFabric.class);
        File root = new File(jarDirectory, "runtimes");
        return new File(root, mode.toString().toLowerCase());

    }

    private HostInfo createHostInfo(String runtimeName, String zoneName, RuntimeMode mode, URI domainName, String environment) throws IOException {

        File runtimeDirectory = getRuntimeDirectory(mode);

        List<File> deployDirs = Collections.emptyList();

        OperatingSystem os = BootstrapHelper.getOperatingSystem();

        DefaultHostInfoBuilder builder = new DefaultHostInfoBuilder();
        builder.runtimeName(runtimeName);
        builder.zoneName(zoneName);
        builder.runtimeMode(mode);
        builder.environment(environment);
        builder.domain(domainName);
        builder.baseDir(runtimeDirectory);
        builder.sharedDirectory(SYNTHETIC_DIRECTORY);
        builder.dataDirectory(dataDirectory);
        builder.tempDirectory(tempDirectory);
        builder.deployDirectories(deployDirs);
        builder.operatingSystem(os);
        return builder.javaEEXAEnabled(false).build();
    }

    /**
     * Scans for extensions on a classpath.
     *
     * @param onlyCore if true, only scan for core extensions; ignore all others as they will be explicitly configured
     * @return the sources
     */
    private ScannedExtensions scanExtensions(boolean onlyCore) throws Fabric3Exception {
        File repositoryDirectory = ArchiveUtils.getJarDirectory(DefaultFabric.class);
        File f3Extensions = new File(repositoryDirectory, "f3.extensions.jar");
        try {

            List<File> archives = scanClasspathForProfileArchives();

            if (archives.size() == 0) {
                throw new Fabric3Exception("Core extension archive not found");
            }

            List<ContributionSource> sources = new ArrayList<>();
            List<File> extensionsFiles = new ArrayList<>();

            for (File extension : archives) {
                // if profiles and/or extensions are explicitly configured, only load the core Fabric extensions and ignore all other extensions/profiles on
                // the classpath
                if ((onlyCore || f3Extensions.exists()) && !extension.getName().contains("fabric3-node-extensions")) {
                    continue;
                }
                extensionsFiles.addAll(ArchiveUtils.unpack(extension, extensionsDirectory));
            }

            // if an f3 extensions archive exists, unzip its contents and add those to the extensions
            if (f3Extensions.exists()) {
                List<File> files = ArchiveUtils.unpack(f3Extensions, extensionsDirectory);
                for (File file : files) {
                    extensionsFiles.add(file);
                }
            }

            addSources(extensionsFiles, sources);

            return new ScannedExtensions(archives, sources);
        } catch (IOException e) {
            throw new Fabric3Exception("Error scanning extensions", e);
        }
    }

    private class ScannedExtensions {
        List<File> profileArchives;
        List<ContributionSource> sources;

        public ScannedExtensions(List<File> profileArchives, List<ContributionSource> sources) {
            this.profileArchives = profileArchives;
            this.sources = sources;
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
        List<File> extensionFiles = new ArrayList<>();
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

