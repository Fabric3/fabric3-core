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
package org.fabric3.plugin.resolver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;

/**
 * Resolves artifacts using Aether.
 */
public class Resolver {
    private RepositorySystem system;
    private RepositorySystemSession session;
    private List<RemoteRepository> repositories;

    private String runtimeVersion;

    public Resolver(RepositorySystem system, RepositorySystemSession session, List<RemoteRepository> repositories, String runtimeVersion) {
        this.system = system;
        this.session = session;
        this.repositories = repositories;
        this.runtimeVersion = runtimeVersion;
    }

    public Set<Artifact> resolveHostArtifacts(Set<Artifact> shared) throws DependencyResolutionException {
        Set<Artifact> hostArtifacts = new HashSet<>();
        Set<Artifact> hostDependencies = Dependencies.getHostDependencies(runtimeVersion);
        for (Artifact artifact : hostDependencies) {
            hostArtifacts.addAll(resolveTransitively(artifact));
        }

        // add shared artifacts and their dependencies to the host classpath
        if (shared != null) {
            for (Artifact artifact : shared) {
                hostArtifacts.addAll(resolveTransitively(artifact));
            }
        }
        return hostArtifacts;
    }

    /**
     * Resolves the modules needed to boot the plugin runtime based on the transitive dependencies of the main runtime module.
     *
     * @return the modules needed to boot the runtime
     * @throws DependencyResolutionException
     */
    public Set<Artifact> resolveRuntimeArtifacts() throws DependencyResolutionException {
        return resolveTransitively(Dependencies.getMainRuntimeModule(runtimeVersion));
    }

    public List<ContributionSource> resolveRuntimeExtensions(Set<Artifact> extensions, Set<Artifact> profiles) throws ArtifactResolutionException {
        Set<Artifact> expandedExtensions = new HashSet<>();
        expandedExtensions.addAll(Dependencies.getCoreExtensions(runtimeVersion));
        expandedExtensions.addAll(extensions);

        Set<Artifact> expandedProfiles = expandProfileExtensions(profiles);
        expandedExtensions.addAll(expandedProfiles);

        Set<URL> extensionUrls = resolve(expandedExtensions);
        return createContributionSources(extensionUrls);
    }

    /**
     * Calculates dependencies based on the set of project artifacts.
     *
     * @param artifacts the set of host artifacts
     * @return the set of URLs pointing to module dependencies.
     */
    public Set<URL> resolveDependencies(Set<Artifact> artifacts) throws DependencyResolutionException {
        Set<URL> urls = new LinkedHashSet<>();
        for (Artifact dependency : artifacts) {
            try {
                Set<Artifact> resolved = resolveTransitively(dependency);
                for (Artifact artifact : resolved) {
                    File pathElement = artifact.getFile();
                    URL url = pathElement.toURI().toURL();
                    urls.add(url);
                }
            } catch (MalformedURLException e) {
                throw new DependencyResolutionException(null, e);
            }

        }
        return urls;
    }

    /**
     * Resolves the set of artifacts and returns their URLs.
     *
     * @param artifacts the artifacts
     * @return the artifact URLs
     * @throws ArtifactResolutionException
     */
    public Set<URL> resolve(Set<Artifact> artifacts) throws ArtifactResolutionException {
        Set<URL> urls = new HashSet<>();
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                Artifact resolvedArtifact = resolveArtifact(artifact);
                try {
                    urls.add(resolvedArtifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AssertionError();
                }
            }
        }
        return urls;
    }

    /**
     * Resolves the artifacts and its transitive dependencies.
     *
     * @param artifact the artifact
     * @return the dependencies
     * @throws DependencyResolutionException
     */
    public Set<Artifact> resolveTransitively(Artifact artifact) throws DependencyResolutionException {
        CollectRequest collectRequest = new CollectRequest();

        Dependency root = new Dependency(artifact, "compile");
        collectRequest.setRoot(root);
        collectRequest.setRepositories(repositories);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

        DependencyResult result = system.resolveDependencies(session, dependencyRequest);

        Set<Artifact> results = new HashSet<>();
        for (ArtifactResult artifactResult : result.getArtifactResults()) {
            Artifact dependency = artifactResult.getArtifact();
            results.add(dependency);
        }
        return results;

    }

    /**
     * Resolves the root dependency to the local artifact.
     *
     * @return Resolved artifact.
     * @throws ArtifactResolutionException if unable to resolve any dependencies.
     */
    public Artifact resolveArtifact(Artifact artifact) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest(artifact, repositories, null);
        ArtifactResult result = system.resolveArtifact(session, request);
        return result.getArtifact();

    }

    /**
     * Returns the set of extensions for the given profiles.
     *
     * @param profiles the profiles
     * @return the expanded profile set including extensions and remote repositories for transitive dependencies
     */
    private Set<Artifact> expandProfileExtensions(Set<Artifact> profiles) throws ArtifactResolutionException {
        Set<Artifact> dependencies = new HashSet<>();
        for (Artifact profile : profiles) {
            try {
                ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();
                request.setArtifact(profile);
                request.setRepositories(repositories);
                ArtifactDescriptorResult result = system.readArtifactDescriptor(session, request);
                List<Exception> exceptions = result.getExceptions();
                if (!exceptions.isEmpty()) {
                    // Maven itself hard-codes exceptions.get(0) so we will do the same  :-(
                    Exception exception = exceptions.get(0);
                    throw new ArtifactResolutionException(null, exception.getMessage(), exception);
                }

                for (Dependency dependency : result.getDependencies()) {

                    dependencies.add(dependency.getArtifact());
                }
            } catch (ArtifactDescriptorException e) {
                throw new ArtifactResolutionException(null, e.getMessage(), e);
            }

        }
        return dependencies;
    }

    private List<ContributionSource> createContributionSources(Set<URL> urls) {
        List<ContributionSource> sources = new ArrayList<>();
        for (URL extensionUrl : urls) {
            // it's ok to assume archives are uniquely named since most server environments have a single deploy directory
            URI uri = URI.create(new File(extensionUrl.getFile()).getName());
            ContributionSource source = new FileContributionSource(uri, extensionUrl, -1, true);
            sources.add(source);
        }
        return sources;
    }

}




