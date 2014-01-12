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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 *
 */
public class ArtifactHelper {
    public ArtifactFactory artifactFactory;
    public ArtifactResolver resolver;
    public ArtifactMetadataSource metadataSource;

    private MavenProject project;
    private ArtifactRepository localRepository;
    private List<ArtifactRepository> remoteRepositories;

    /**
     * Sets the local repository.
     *
     * @param localRepository the local repository
     */
    public void setLocalRepository(ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    /**
     * Sets the Maven project.
     *
     * @param project the Maven project.
     */
    public void setProject(MavenProject project) {
        this.project = project;
        this.remoteRepositories = project.getRemoteArtifactRepositories();
    }

    public Set<Artifact> calculateRuntimeArtifacts(String runtimeVersion, int mavenVersion) throws MojoExecutionException {
        List<Exclusion> exclusions = Collections.emptyList();
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-maven3-host");
        dependency.setVersion(runtimeVersion);
        dependency.setExclusions(exclusions);
        return resolveArtifacts(dependency, Collections.<ArtifactRepository>emptySet(), true);
    }

    /**
     * Calculates module dependencies based on the set of project artifacts. Module dependencies must be visible to implementation code in a composite and
     * encompass project artifacts minus artifacts provided by the host classloader and those that are "provided scope".
     *
     * @param projectArtifacts the artifact set to determine module dependencies from
     * @param hostArtifacts    the set of host artifacts
     * @return the set of URLs pointing to module dependencies.
     */
    public Set<URL> calculateModuleDependencies(Set<Artifact> projectArtifacts, Set<Artifact> hostArtifacts) {
        Set<URL> urls = new LinkedHashSet<URL>();
        for (Artifact artifact : projectArtifacts) {
            try {
                String scope = artifact.getScope();
                if (hostArtifacts.contains(artifact) || Artifact.SCOPE_PROVIDED.equals(scope)) {
                    continue;
                }
                File pathElement = artifact.getFile();
                URL url = pathElement.toURI().toURL();
                urls.add(url);
            } catch (MalformedURLException e) {
                // toURI should have encoded the URL
                throw new AssertionError(e);
            }

        }
        return urls;
    }

    @SuppressWarnings({"unchecked"})
    public Set<Artifact> calculateDependencies() throws MojoExecutionException {
        // add all declared project dependencies
        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (Dependency dependency : project.getDependencies()) {
            if (!dependency.getScope().equals("f3-extension")) {
                artifacts.addAll(resolveArtifacts(dependency, Collections.<ArtifactRepository>emptySet(), true));
            }
        }

        // include any artifacts that have been added by other plugins (e.g. Clover see FABRICTHREE-220)
        for (Artifact artifact : project.getDependencyArtifacts()) {
            if (!artifact.getScope().equals("f3-extension")) {
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

    /**
     * Transitively calculates the set of artifacts to be included in the host classloader based on the artifacts associated with the Maven module.
     *
     * @param runtimeArtifacts the artifacts associated with the Maven module
     * @param shared           the dependencies shared between all user and extension contributions
     * @return the set of artifacts to be included in the host classloader
     * @throws MojoExecutionException if an error occurs calculating the transitive dependencies
     */
    public Set<Artifact> calculateHostArtifacts(Set<Artifact> runtimeArtifacts, Dependency[] shared) throws MojoExecutionException {

        Set<Artifact> hostArtifacts = new HashSet<Artifact>();
        List<Exclusion> exclusions = Collections.emptyList();
        // find the version of fabric3-api being used by the runtime
        String version = null;
        for (Artifact artifact : runtimeArtifacts) {
            if ("org.codehaus.fabric3".equals(artifact.getGroupId()) && "fabric3-api".equals(artifact.getArtifactId())) {
                version = artifact.getVersion();
                break;
            }
        }
        if (version == null) {
            throw new MojoExecutionException("org.codehaus.fabric3:fabric3-api version not found");
        }
        // add transitive dependencies of fabric3-api to the list of artifacts in the host classloader
        Dependency fabric3Api = new Dependency();
        fabric3Api.setGroupId("org.codehaus.fabric3");
        fabric3Api.setArtifactId("fabric3-api");
        fabric3Api.setVersion(version);
        fabric3Api.setExclusions(exclusions);
        hostArtifacts.addAll(resolveArtifacts(fabric3Api, Collections.<ArtifactRepository>emptySet(), true));

        // add commons annotations dependency
        Dependency jsr250API = new Dependency();
        jsr250API.setGroupId("javax.annotation");
        jsr250API.setArtifactId("javax.annotation-api");
        jsr250API.setVersion("1.2");
        hostArtifacts.addAll(resolveArtifacts(jsr250API, Collections.<ArtifactRepository>emptySet(), true));

        // add JAXB API dependency
        Dependency jaxbAPI = new Dependency();
        jaxbAPI.setGroupId("javax.xml.bind");
        jaxbAPI.setArtifactId("jaxb-api-osgi");
        jaxbAPI.setVersion("2.2-promoted-b50");
        hostArtifacts.addAll(resolveArtifacts(jaxbAPI, Collections.<ArtifactRepository>emptySet(), true));

        // add JAX-RS API
        Dependency rsAPI = new Dependency();
        rsAPI.setGroupId("javax.ws.rs");
        rsAPI.setArtifactId("javax.ws.rs-api");
        rsAPI.setVersion("2.0");
        hostArtifacts.addAll(resolveArtifacts(rsAPI, Collections.<ArtifactRepository>emptySet(), true));

        // add Node API
        Dependency nodeAPI = new Dependency();
        nodeAPI.setGroupId("org.codehaus.fabric3");
        nodeAPI.setArtifactId("fabric3-node-api");
        nodeAPI.setVersion(version);
        hostArtifacts.addAll(resolveArtifacts(nodeAPI, Collections.<ArtifactRepository>emptySet(), true));

        // add shared artifacts to the host classpath
        if (shared != null) {
            for (Dependency sharedDependency : shared) {
                hostArtifacts.addAll(resolveArtifacts(sharedDependency, Collections.<ArtifactRepository>emptySet(), true));
            }
        }
        return hostArtifacts;
    }

    /**
     * Returns the set of extensions for the given profiles.
     *
     * @param profiles the profiles
     * @return the expanded profile set including extensions and remote repositories for transitive dependencies
     * @throws MojoExecutionException if there is an error dereferencing the extensions
     */
    @SuppressWarnings({"unchecked"})
    public ExpandedProfiles expandProfileExtensions(Dependency[] profiles) throws MojoExecutionException {
        Set<Dependency> dependencies = new HashSet<Dependency>();
        Set<ArtifactRepository> repositories = new HashSet<ArtifactRepository>();
        try {
            for (Dependency profile : profiles) {
                Artifact artifact = artifactFactory.createArtifact(profile.getGroupId(), profile.getArtifactId(), profile.getVersion(), "compile", "jar");
                ResolutionGroup resolutionGroup = metadataSource.retrieve(artifact, localRepository, remoteRepositories);
                Set<Artifact> extensions = resolutionGroup.getArtifacts();
                for (Artifact extension : extensions) {
                    Dependency dependency = new Dependency();
                    dependency.setGroupId(extension.getGroupId());
                    dependency.setArtifactId(extension.getArtifactId());
                    dependency.setVersion(extension.getVersion());
                    dependencies.add(dependency);
                }
                for (ArtifactRepository repository : resolutionGroup.getResolutionRepositories()) {
                    repositories.add(repository);
                }
            }
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        return new ExpandedProfiles(dependencies, repositories);
    }

    /**
     * Resolves the root dependency to the local artifact.
     *
     * @param dependency   Root dependency.
     * @param repositories additional remote repositories to resolve transitive dependencies
     * @return Resolved artifact.
     * @throws MojoExecutionException if unable to resolve any dependencies.
     */
    public Artifact resolve(Dependency dependency, Set<ArtifactRepository> repositories) throws MojoExecutionException {
        return resolveArtifacts(dependency, repositories, false).iterator().next();
    }

    /**
     * Resolves dependencies for a dependency.
     *
     * @param dependency   Root dependency.
     * @param repositories additional remote repositories to resolve transitive dependencies
     * @param transitive   true if the resolution should be performed transitively  @return Resolved set of artifacts.
     * @return repositories to resolve dependencies
     * @throws MojoExecutionException if unable to resolve any dependencies.
     */
    @SuppressWarnings({"unchecked"})
    private Set<Artifact> resolveArtifacts(Dependency dependency, Set<ArtifactRepository> repositories, boolean transitive) throws MojoExecutionException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        if (dependency.getVersion() == null) {
            resolveVersion(dependency);
        }
        List<Exclusion> exclusions = dependency.getExclusions();
        Artifact rootArtifact = createArtifact(dependency);
        try {
            List<ArtifactRepository> dependencyRepositories = new ArrayList<ArtifactRepository>(remoteRepositories);
            dependencyRepositories.addAll(repositories);
            resolver.resolve(rootArtifact, dependencyRepositories, localRepository);
            artifacts.add(rootArtifact);
            if (!transitive) {
                return artifacts;
            }
            Set<Artifact> resolvedArtifacts = resolveTransitive(exclusions, rootArtifact);
            artifacts.addAll(resolvedArtifacts);
            return artifacts;
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Artifact> resolveTransitive(final List<Exclusion> exclusions, Artifact rootArtifact) throws MojoExecutionException {
        try {
            ResolutionGroup resolutionGroup = metadataSource.retrieve(rootArtifact, localRepository, remoteRepositories);
            ArtifactFilter filter = new ArtifactFilter() {
                public boolean include(Artifact artifact) {
                    String groupId = artifact.getGroupId();
                    String artifactId = artifact.getArtifactId();

                    for (Exclusion exclusion : exclusions) {
                        if (artifactId.equals(exclusion.getArtifactId()) && groupId.equals(exclusion.getGroupId())) {
                            return false;
                        }
                    }
                    return true;
                }
            };
            Set artifacts = resolutionGroup.getArtifacts();
            ArtifactResolutionResult result = resolver.resolveTransitively(artifacts,
                                                                           rootArtifact,
                                                                           Collections.emptyMap(),
                                                                           localRepository,
                                                                           remoteRepositories,
                                                                           metadataSource,
                                                                           filter);
            return result.getArtifacts();

        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    /**
     * Resolves the dependency version based on the project managed dependencies.
     *
     * @param dependency the project dependency
     */
    @SuppressWarnings({"unchecked"})
    private void resolveVersion(Dependency dependency) {
        List<Dependency> managedDependencies = project.getDependencyManagement().getDependencies();
        for (Dependency managedDependency : managedDependencies) {
            String groupId = managedDependency.getGroupId();
            String artifactId = managedDependency.getArtifactId();
            if (groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())) {
                dependency.setVersion(managedDependency.getVersion());
            }
        }
    }

    /**
     * Creates an artifact from the dependency.
     *
     * @param dependency the dependency
     * @return the artifact
     */
    private Artifact createArtifact(Dependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();
        String type = dependency.getType();
        String classifier = dependency.getClassifier();
        if (classifier == null) {
            return artifactFactory.createArtifact(groupId, artifactId, version, Artifact.SCOPE_RUNTIME, type);
        } else {
            return artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
        }
    }

}
