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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.contribution.manifest.ContributionExport;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.contribution.UnresolvedImportException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

import static org.fabric3.host.Names.HOST_CONTRIBUTION;

/**
 * Default implementation of the ContributionLoader. Classloaders corresponding to loaded contributions are registered by name with the system
 * ClassLoaderRegistry.
 *
 * @version $Rev$ $Date$
 */
public class ContributionLoaderImpl implements ContributionLoader {
    private final ContributionImport hostImport;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final MetaDataStore store;
    private final ClasspathProcessorRegistry classpathProcessorRegistry;
    private boolean classloaderIsolation;
    private Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators;
    private ClassLoaderWireBuilder builder;
    private Field sysPathsField;

    public ContributionLoaderImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference MetaDataStore store,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators,
                                  @Reference ClassLoaderWireBuilder builder,
                                  @Reference HostInfo info,
                                  @Monitor ContributionLoaderMonitor monitor) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.store = store;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.generators = generators;
        this.builder = builder;
        classloaderIsolation = info.supportsClassLoaderIsolation();
        hostImport = new ContributionImport(HOST_CONTRIBUTION);
        ContributionExport hostExport = new ContributionExport(HOST_CONTRIBUTION);
        hostExport.resolve();
        hostImport.addResolved(HOST_CONTRIBUTION, hostExport);
        try {
            sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // sys_paths not available on some JVMs, e.g. IBM J9
            monitor.nativeLibrariesNotSupported();
        }
    }

    public ClassLoader load(Contribution contribution) throws ContributionLoadException, UnresolvedImportException {
        URI contributionUri = contribution.getUri();
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CONTRIBUTION);
        // all contributions implicitly import the host contribution
        ContributionManifest manifest = contribution.getManifest();
        manifest.addImport(hostImport);

        // verify and resolve the imports
        List<ContributionWire<?, ?>> wires = resolveImports(contribution);
        if (!classloaderIsolation) {
            // the host environment does not support classloader isolation, return the host classloader
            return hostClassLoader;
        }
        MultiParentClassLoader loader = new MultiParentClassLoader(contributionUri, hostClassLoader);
        // construct the classpath for contained resources in the contribution if it is a physical artifact
        URL location = contribution.getLocation();
        if (location != null) {
            try {
                List<URL> classpath = classpathProcessorRegistry.process(location, manifest.getLibraries());
                for (URL url : classpath) {
                    loader.addURL(url);
                }
                setSysPathsField(loader);
            } catch (IOException e) {
                throw new ContributionLoadException(e);
            }

        }

        // connect imported contribution classloaders according to their wires
        for (ContributionWire<?, ?> wire : wires) {
            ClassLoaderWireGenerator generator = generators.get(wire.getClass());
            if (generator == null) {
                // not all contribution wires resolve resources through classloaders, so skip if one is not found
                continue;
            }
            PhysicalClassLoaderWireDefinition wireDefinition = generator.generate(wire);
            builder.build(loader, wireDefinition);
        }

        // add contributions that extend extension points provided by this contribution
        List<URI> extenders = resolveExtensionProviders(contribution);
        for (URI uri : extenders) {
            ClassLoader cl = classLoaderRegistry.getClassLoader(uri);
            if (cl == null) {
                // the extension provider may not have been loaded yet
                continue;
            }
            if (!(cl instanceof MultiParentClassLoader)) {
                throw new AssertionError("Extension point provider classloader must be a " + MultiParentClassLoader.class.getName());
            }
            loader.addExtensionClassLoader((MultiParentClassLoader) cl);
        }
        // add this contribution to extension points it extends
        List<URI> extensionPoints = resolveExtensionPoints(contribution);
        for (URI uri : extensionPoints) {
            ClassLoader cl = classLoaderRegistry.getClassLoader(uri);
            if (cl == null) {
                // the extension point may not have been loaded yet
                continue;
            }
            if (!(cl instanceof MultiParentClassLoader)) {
                throw new AssertionError("Extension point classloader must be a " + MultiParentClassLoader.class.getName());
            }
            ((MultiParentClassLoader) cl).addExtensionClassLoader(loader);
        }

        // register the classloader
        classLoaderRegistry.register(contributionUri, loader);
        return loader;
    }

    public void unload(Contribution contribution) throws ContributionInUseException {
        URI uri = contribution.getUri();
        Set<Contribution> contributions = store.resolveDependentContributions(uri);
        if (!contributions.isEmpty()) {
            Set<URI> dependents = new HashSet<URI>(contributions.size());
            for (Contribution dependent : contributions) {
                if (ContributionState.INSTALLED == dependent.getState()) {
                    dependents.add(dependent.getUri());
                }
            }
            if (!dependents.isEmpty()) {
                throw new ContributionInUseException("Contribution is in use: " + uri, uri, dependents);
            }
        }
        classLoaderRegistry.unregister(uri);
    }


    private List<ContributionWire<?, ?>> resolveImports(Contribution contribution) throws UnresolvedImportException {
        // clear the wires as the contribution may have been loaded previously
        contribution.getWires().clear();
        List<ContributionWire<?, ?>> resolved = new ArrayList<ContributionWire<?, ?>>();
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            URI uri = contribution.getUri();
            try {
                List<ContributionWire<?, ?>> wires = store.resolveContributionWires(uri, imprt);
                // add the resolved wire to the contribution
                for (ContributionWire<?, ?> wire : wires) {
                    contribution.addWire(wire);
                    resolved.add(wire);
                }
            } catch (UnresolvedImportException e) {
                if (!imprt.isRequired()) {
                    // not required, ignore
                    continue;
                }
                throw e;
            }
        }
        return resolved;
    }

    private List<URI> resolveExtensionProviders(Contribution contribution) {
        List<URI> uris = new ArrayList<URI>();
        ContributionManifest manifest = contribution.getManifest();
        for (String extensionPoint : manifest.getExtensionPoints()) {
            List<Contribution> providers = store.resolveExtensionProviders(extensionPoint);
            for (Contribution provider : providers) {
                uris.add(provider.getUri());
                contribution.addResolvedExtensionProvider(provider.getUri());
            }
        }
        return uris;
    }

    private List<URI> resolveExtensionPoints(Contribution contribution) {
        List<URI> uris = new ArrayList<URI>();
        ContributionManifest manifest = contribution.getManifest();
        for (String extend : manifest.getExtends()) {
            List<Contribution> extensionPoints = store.resolveExtensionPoints(extend);
            for (Contribution extensionPoint : extensionPoints) {
                uris.add(extensionPoint.getUri());
                extensionPoint.addResolvedExtensionProvider(contribution.getUri());
            }
        }
        return uris;
    }

    /**
     * Sets the native libraries path by setting the classloader sysPathsField to null. This will force the classloader to reinitialize the field to
     * JAVA_LIBRARY_PATH.
     *
     * @param loader the classloader
     */
    private void setSysPathsField(MultiParentClassLoader loader) {
        if (sysPathsField == null) {
            return;
        }
        try {
            sysPathsField.set(loader, null);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }


}
