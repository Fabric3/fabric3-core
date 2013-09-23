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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.fabric3.api.node.Domain;
import org.fabric3.api.node.Fabric;
import org.fabric3.api.node.FabricException;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.classloader.MaskingClassLoader;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.monitor.DelegatingDestinationRouter;
import org.fabric3.host.os.OperatingSystem;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapFactory;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.DefaultHostInfo;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HiddenPackages;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScanException;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.host.util.FileHelper;
import org.w3c.dom.Document;

/**
 * The default implementation of a fabric connection.
 */
public class DefaultFabric implements Fabric {
    private static final File SYNTHETIC_DIRECTORY = new File("notfound");
    private static final String SYSTEM_COMPOSITE = "META-INF/system.composite";
    private static final URI DOMAIN_URI = URI.create("fabric3://runtime/NodeDomain");

    private enum State {
        UNINITIALIZED, INITIALIZED, STARTED, STOPPED
    }

    private File tempDirectory = new File(System.getProperty("java.io.tmpdir"), ".f3");
    private File extensionsDirectory = new File(tempDirectory, "extensions");
    private File dataDirectory = new File(tempDirectory, "data");

    private State state = State.UNINITIALIZED;

    private Fabric3Runtime runtime;
    private RuntimeCoordinator coordinator;

    private Domain domain;

    /**
     * Constructor.
     *
     * @param configUrl the system configuration. If null, this implementation attempts to resolve and use the default configuration.
     */
    public DefaultFabric(URL configUrl) {
        long start = System.currentTimeMillis();

        DelegatingDestinationRouter router = new DelegatingDestinationRouter();

        try {
            createDirectories();

            // create the classloaders for booting the runtime
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader maskingClassLoader = new MaskingClassLoader(systemClassLoader, HiddenPackages.getPackages());
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, SYNTHETIC_DIRECTORY);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, SYNTHETIC_DIRECTORY);

            BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

            // load the system configuration
            UrlSource urlSource = resolveConfiguration(configUrl);
            Document systemConfig = bootstrapService.loadSystemConfig(urlSource);

            URI domainName = bootstrapService.parseDomainName(systemConfig);

            RuntimeMode mode = bootstrapService.parseRuntimeMode(systemConfig);

            String environment = bootstrapService.parseEnvironment(systemConfig);

            String zoneName = bootstrapService.parseZoneName(systemConfig);

            String defaultRuntimeName = UUID.randomUUID().toString();
            String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, defaultRuntimeName, mode);

            URL systemComposite = getClass().getClassLoader().getResource(SYSTEM_COMPOSITE);

            // create the HostInfo and runtime
            HostInfo hostInfo = createHostInfo(runtimeName, mode, domainName, environment);

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, null, router);

            runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            // scan the classpath for extension archives
            List<ContributionSource> extensions = scanExtensions();

            BootConfiguration configuration = new BootConfiguration();
            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(hostLoader);
            configuration.setBootClassLoader(bootLoader);
            configuration.setSystemCompositeUrl(systemComposite);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(extensions);

            // boot the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.boot();

            state = State.INITIALIZED;

            System.out.println("Booted in: " + (System.currentTimeMillis() - start));
        } catch (RuntimeException e) {
            router.flush(System.out);
            throw new FabricException(e);
        } catch (Exception e) {
            router.flush(System.out);
            throw new FabricException(e);
        }
    }

    public void start() throws FabricException {
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("Not in initialized state: " + state);
        }
        try {
            coordinator.load();
            coordinator.joinDomain();
            state = State.STARTED;
        } catch (InitializationException e) {
            throw new FabricException(e);
        }
    }

    public void stop() throws FabricException {
        if (state != State.STARTED) {
            throw new IllegalStateException("Not in started state: " + state);
        }
        try {
            coordinator.shutdown();
            state = State.STOPPED;
        } catch (ShutdownException e) {
            throw new FabricException(e);
        }
    }

    public <T> void registerSystemService(Class<T> interfaze, T instance) throws FabricException {

    }

    public Domain getDomain() {
        if (domain == null) {
            domain = runtime.getComponent(Domain.class, DOMAIN_URI);
        }
        return domain;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectories() throws IOException {
        // clear out the tmp directory
        if (tempDirectory.exists()) {
            FileHelper.cleanDirectory(tempDirectory);
        }
        tempDirectory.mkdirs();
        extensionsDirectory.mkdirs();
        dataDirectory.mkdirs();
    }

    private UrlSource resolveConfiguration(URL configUrl) {
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

    public HostInfo createHostInfo(String runtimeName, RuntimeMode mode, URI domainName, String environment) throws IOException {

        File runtimeDirectory = getRuntimeDirectory(mode);

        List<File> deployDirs = Collections.emptyList();

        OperatingSystem os = BootstrapHelper.getOperatingSystem();

        return new DefaultHostInfo(runtimeName,
                                   mode,
                                   environment,
                                   domainName,
                                   runtimeDirectory,
                                   SYNTHETIC_DIRECTORY,
                                   SYNTHETIC_DIRECTORY,
                                   runtimeDirectory,
                                   SYNTHETIC_DIRECTORY,
                                   dataDirectory,
                                   tempDirectory,
                                   deployDirs,
                                   os,
                                   false);
    }

    private List<ContributionSource> scanExtensions() throws ScanException {
        try {

            List<File> extensions = getExtensionArchives();

            if (extensions.size() == 0) {
                throw new ScanException("Core extensions not found");
            }

            for (File extension : extensions) {
                ArchiveUtils.unpack(extension, extensionsDirectory);
            }
            File[] files = extensionsDirectory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    // skip directories and files beginning with '.'
                    return !pathname.getName().startsWith(".");
                }
            });

            if (files == null) {
                return Collections.emptyList();
            }

            List<ContributionSource> sources = new ArrayList<ContributionSource>();
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
            return sources;
        } catch (IOException e) {
            throw new ScanException("Error scanning extensions", e);
        }
    }

    /**
     * Returns the installed extensions by scanning the classpath for F3 manifest entries.
     *
     * @return a collection containing the archive files
     * @throws IOException if there is an error scanning for extensions
     */
    private List<File> getExtensionArchives() throws IOException {
        Enumeration<URL> manifests = getClass().getClassLoader().getResources("extensions/F3-MANIFEST.MF");
        List<File> extensions = new ArrayList<File>();
        while (manifests.hasMoreElements()) {
            // determine the containing archive name by removing the jar:file: protocol prefix and the manifest suffix
            URL manifest = manifests.nextElement();
            String path = manifest.getPath();
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }
            path = path.substring(0, path.length() - 27);      //27  = "!/extensions/F3-MANIFEST.MF"
            extensions.add(new File(path));
        }
        return extensions;
    }

}

