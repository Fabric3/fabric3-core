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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
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
 * Performs artifact resolution for extensions and modules.
 */
public class Resolver {
    private MavenProject project;
    private String runtimeVersion;

    private RepositorySystem repositorySystem;
    private RepositorySystemSession session;
    private List<RemoteRepository> projectRepositories;

    public Resolver(MavenProject project,
                    String runtimeVersion,
                    RepositorySystem repositorySystem,
                    RepositorySystemSession session,
                    List<RemoteRepository> projectRepositories) {
        this.project = project;
        this.runtimeVersion = runtimeVersion;
        this.repositorySystem = repositorySystem;
        this.session = session;
        this.projectRepositories = projectRepositories;
    }

    /**
     * Resolves the main runtime modules.
     *
     * @return the main runtime modules
     * @throws MojoExecutionException
     */
    public Set<Artifact> resolveRuntimeArtifacts() throws MojoExecutionException {
        return resolveArtifacts(Dependencies.getMainRuntimeModule(runtimeVersion));
    }

    /**
     * Transitively calculates the set of artifacts to be included in the host classloader based on the artifacts associated with the Maven module.
     *
     * @param shared the dependencies shared between user and extension contributions
     * @return the set of artifacts to be included in the host classloader
     * @throws MojoExecutionException if an error occurs calculating the transitive dependencies
     */
    public Set<Artifact> resolveHostArtifacts(org.apache.maven.model.Dependency[] shared) throws MojoExecutionException {

        Set<Artifact> hostArtifacts = new HashSet<>();
        List<org.apache.maven.model.Dependency> hostDependencies = Dependencies.getHostDependencies(runtimeVersion);
        for (org.apache.maven.model.Dependency dependency : hostDependencies) {
            hostArtifacts.addAll(resolveArtifacts(dependency));
        }

        // add shared artifacts and their dependencies to the host classpath
        if (shared != null) {
            for (org.apache.maven.model.Dependency sharedDependency : shared) {
                hostArtifacts.addAll(resolveArtifacts(sharedDependency));
            }
        }
        return hostArtifacts;
    }

    /**
     * Calculates module dependencies based on the set of project artifacts. Module dependencies must be visible to implementation code in a composite and
     * encompass project artifacts minus artifacts provided by the host classloader and those that are "provided scope".
     *
     * @param hostArtifacts the set of host artifacts
     * @return the set of URLs pointing to module dependencies.
     */
    public Set<URL> resolveModuleDependencies(Set<Artifact> hostArtifacts) throws MojoExecutionException {
        Set<Dependency> projectDependencies = calculateProjectDependencies();
        Set<URL> urls = new LinkedHashSet<>();
        for (Dependency dependency : projectDependencies) {
            try {
                String scope = dependency.getScope();
                if (hostArtifacts.contains(dependency.getArtifact()) || "provided".equals(scope)) {
                    continue;
                }

                File pathElement = resolve(dependency.getArtifact()).getFile();
                URL url = pathElement.toURI().toURL();
                urls.add(url);
            } catch (MalformedURLException | ArtifactResolutionException e) {
                // toURI should have encoded the URL
                throw new MojoExecutionException(e.getMessage(), e);
            }

        }
        return urls;
    }

    /**
     * Resolves runtime extensions and extensions included in profiles
     *
     * @param extensions runtime extensions
     * @param profiles   runtime profiles
     * @return sources for deploying runtime extensions and extensions contained in profiles
     * @throws MojoExecutionException
     */
    public List<ContributionSource> resolveRuntimeExtensions(org.apache.maven.model.Dependency[] extensions, org.apache.maven.model.Dependency[] profiles)
            throws MojoExecutionException {
        Set<org.apache.maven.model.Dependency> expandedExtensions = new HashSet<>();
        expandedExtensions.addAll(Dependencies.getCoreExtensions(runtimeVersion));
        expandedExtensions.addAll(Arrays.asList(extensions));

        Set<org.apache.maven.model.Dependency> expandedProfiles = expandProfileExtensions(profiles);
        expandedExtensions.addAll(expandedProfiles);

        Set<URL> extensionUrls = resolve(expandedExtensions);
        return createContributionSources(extensionUrls);
    }

    /**
     * Resolves the root dependency to the local artifact.
     *
     * @param dependency Root dependency.
     * @return Resolved artifact.
     * @throws MojoExecutionException if unable to resolve any dependencies.
     */
    public Artifact resolve(org.apache.maven.model.Dependency dependency) throws MojoExecutionException {
        try {
            Artifact artifact = convertToArtifact(dependency);
            return resolve(artifact);
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Set<URL> resolve(Set<org.apache.maven.model.Dependency> dependencies) throws MojoExecutionException {
        Set<URL> urls = new HashSet<>();
        if (dependencies != null) {
            for (org.apache.maven.model.Dependency dependency : dependencies) {
                Artifact artifact = resolve(dependency);
                try {
                    urls.add(artifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AssertionError();
                }
            }
        }
        return urls;
    }

    /**
     * Returns the set of extensions for the given profiles.
     *
     * @param profiles the profiles
     * @return the expanded profile set including extensions and remote repositories for transitive dependencies
     * @throws MojoExecutionException if there is an error dereferencing the extensions
     */
    private Set<org.apache.maven.model.Dependency> expandProfileExtensions(org.apache.maven.model.Dependency[] profiles) throws MojoExecutionException {
        Set<org.apache.maven.model.Dependency> dependencies = new HashSet<>();
        for (org.apache.maven.model.Dependency profile : profiles) {
            profile.setType("pom");  // if type not explicitly set, may be JAR
            try {
                ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();
                request.setArtifact(convertToArtifact(profile));
                request.setRepositories(projectRepositories);
                ArtifactDescriptorResult result = repositorySystem.readArtifactDescriptor(session, request);
                List<Exception> exceptions = result.getExceptions();
                if (!exceptions.isEmpty()) {
                    // Maven itself hard-codes exceptions.get(0) so we will do the same  :-(
                    Exception exception = exceptions.get(0);
                    throw new MojoExecutionException(exception.getMessage(), exception);
                }

                for (Dependency dependency : result.getDependencies()) {

                    org.apache.maven.model.Dependency mavenDependency = new org.apache.maven.model.Dependency();
                    Artifact artifact = dependency.getArtifact();
                    mavenDependency.setArtifactId(artifact.getArtifactId());
                    mavenDependency.setGroupId(artifact.getGroupId());
                    mavenDependency.setVersion(artifact.getVersion());
                    mavenDependency.setClassifier(artifact.getClassifier());
                    mavenDependency.setType(artifact.getExtension());
                    dependencies.add(mavenDependency);
                }
            } catch (ArtifactDescriptorException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

        }
        return dependencies;
    }

    private Set<Dependency> calculateProjectDependencies() throws MojoExecutionException {
        // add all declared project dependencies
        Set<Dependency> artifacts = new HashSet<>();
        for (org.apache.maven.model.Dependency dependency : project.getDependencies()) {
            Set<Artifact> resolved = resolveArtifacts(dependency);
            for (Artifact artifact : resolved) {
                artifacts.add(new Dependency(artifact, dependency.getScope()));
            }
        }

        // include any artifacts that have been added by other plugins (e.g. Clover see FABRICTHREE-220)
        for (org.apache.maven.artifact.Artifact artifact : project.getDependencyArtifacts()) {
            artifacts.add(new Dependency(convertArtifact(artifact), artifact.getScope()));
        }
        return artifacts;
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

    private Artifact resolve(Artifact artifact) throws ArtifactResolutionException {
        ArtifactResult result = repositorySystem.resolveArtifact(session, new ArtifactRequest(artifact, projectRepositories, null));
        return result.getArtifact();
    }

    private Set<Artifact> resolveArtifacts(org.apache.maven.model.Dependency dependency) throws MojoExecutionException {
        try {
            CollectRequest collectRequest = new CollectRequest();

            Artifact converted = convertToArtifact(dependency);
            Dependency root = new Dependency(converted, dependency.getScope());
            collectRequest.setRoot(root);
            collectRequest.setRepositories(projectRepositories);
            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

            DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);

            Set<Artifact> results = new HashSet<>();
            for (ArtifactResult artifactResult : result.getArtifactResults()) {
                Artifact artifact = artifactResult.getArtifact();
                results.add(artifact);
            }
            return results;

        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private Artifact convertToArtifact(org.apache.maven.model.Dependency dependency) {
        String version = dependency.getVersion();
        if (version == null) {
            version = resolveVersion(dependency);
        }
        return new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), version);
    }

    private DefaultArtifact convertArtifact(org.apache.maven.artifact.Artifact artifact) {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getType(), artifact.getVersion());
    }

    /**
     * Resolves the dependency version based on the project managed dependencies.
     *
     * @param dependency the project dependency
     */
    private String resolveVersion(org.apache.maven.model.Dependency dependency) {
        List<org.apache.maven.model.Dependency> managedDependencies = project.getDependencyManagement().getDependencies();
        for (org.apache.maven.model.Dependency managedDependency : managedDependencies) {
            String groupId = managedDependency.getGroupId();
            String artifactId = managedDependency.getArtifactId();
            if (groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())) {
                return managedDependency.getVersion();
            }
        }
        return null;
    }

}
