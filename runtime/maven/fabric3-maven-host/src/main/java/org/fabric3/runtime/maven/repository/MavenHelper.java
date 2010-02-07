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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.repository;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DefaultClassRealm;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;

import org.fabric3.host.repository.RepositoryException;

/**
 * Utility class for embedding Maven.
 *
 * @version $Rev$ $Date$
 */
public class MavenHelper {
    private final String[] remoteRepositoryUrls;
    private ArtifactMetadataSource metadataSource;
    private ArtifactFactory artifactFactory;
    private ArtifactRepository localRepository;
    private List<ArtifactRepository> remoteRepositories = new LinkedList<ArtifactRepository>();
    private List<ArtifactRepository> remoteMirrors = new LinkedList<ArtifactRepository>();
    private ArtifactResolver artifactResolver;
    private boolean online;

    /**
     * Initialize the remote repository URLs.
     *
     * @param remoteRepositoryUrl Remote repository URLS.
     * @param online              whether the runtime is online or not
     */
    public MavenHelper(String remoteRepositoryUrl, boolean online) {
        this.remoteRepositoryUrls = remoteRepositoryUrl.split(",");
        this.online = online;
    }

    /**
     * Starts the embedder.
     *
     * @throws RepositoryException If unable to start the embedder.
     */
    public void start() throws RepositoryException {

        try {

            Embedder embedder = new Embedder();
            ClassWorld classWorld = new ClassWorld();

            classWorld.newRealm("plexus.fabric", getClass().getClassLoader());

            // Evil hack for Tomcat classloader issue - starts
            Field realmsField = ClassWorld.class.getDeclaredField("realms");
            realmsField.setAccessible(true);
            Map realms = (Map) realmsField.get(classWorld);
            DefaultClassRealm realm = (DefaultClassRealm) realms.get("plexus.fabric");

            Class clazz = Class.forName("org.codehaus.classworlds.RealmClassLoader");
            Constructor ctr = clazz.getDeclaredConstructor(DefaultClassRealm.class, ClassLoader.class);
            ctr.setAccessible(true);
            Object realmClassLoader = ctr.newInstance(realm, getClass().getClassLoader());

            Field realmClassLoaderField = DefaultClassRealm.class.getDeclaredField("classLoader");
            realmClassLoaderField.setAccessible(true);
            realmClassLoaderField.set(realm, realmClassLoader);
            // Evil hack for Tomcat classloader issue - ends

            embedder.start(classWorld);

            metadataSource = (ArtifactMetadataSource) embedder.lookup(ArtifactMetadataSource.ROLE);
            artifactFactory = (ArtifactFactory) embedder.lookup(ArtifactFactory.ROLE);
            artifactResolver = (ArtifactResolver) embedder.lookup(ArtifactResolver.ROLE);

            setUpRepositories(embedder);

            embedder.stop();

        } catch (DuplicateRealmException ex) {
            throw new RepositoryException(ex);
        } catch (PlexusContainerException ex) {
            throw new RepositoryException(ex);
        } catch (ComponentLookupException ex) {
            throw new RepositoryException(ex);
        } catch (NoSuchFieldException ex) {
            throw new RepositoryException(ex);
        } catch (IllegalAccessException ex) {
            throw new RepositoryException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RepositoryException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RepositoryException(ex);
        } catch (InstantiationException ex) {
            throw new RepositoryException(ex);
        } catch (InvocationTargetException ex) {
            throw new RepositoryException(ex);
        }

    }

    /**
     * Resolves the dependencies transitively.
     *
     * @param rootArtifact Artifact whose dependencies need to be resolved.
     * @return true if the artifact was succesfully resolved
     * @throws RepositoryException If unable to resolve the dependencies.
     */
    public boolean resolveTransitively(Artifact rootArtifact) throws RepositoryException {

        org.apache.maven.artifact.Artifact artifact = artifactFactory.createArtifact(rootArtifact.getGroup(),
                                                                                     rootArtifact.getName(),
                                                                                     rootArtifact.getVersion(),
                                                                                     SCOPE_RUNTIME,
                                                                                     rootArtifact.getType());
        try {
            if (resolve(artifact)) {
                rootArtifact.setUrl(artifact.getFile().toURL());
                return resolveDependencies(rootArtifact, artifact);
            } else {
                return false;
            }
        } catch (MalformedURLException ex) {
            throw new RepositoryException(ex);
        }

    }

    /**
     * Resolves the artifact.
     *
     * @param artifact the root artifact
     * @return returns true if the artifact was resolved successfully
     */
    private boolean resolve(org.apache.maven.artifact.Artifact artifact) {

        try {
            artifactResolver.resolve(artifact, remoteRepositories, localRepository);
            return true;
        } catch (ArtifactResolutionException ex) {
            return false;
        } catch (ArtifactNotFoundException ex) {
            return false;
        }

    }

    /**
     * Sets up local and remote repositories.
     *
     * @param embedder the embedder
     * @throws RepositoryException if an error occurs
     */
    private void setUpRepositories(Embedder embedder) throws RepositoryException {

        try {

            ArtifactRepositoryFactory artifactRepositoryFactory =
                    (ArtifactRepositoryFactory) embedder.lookup(ArtifactRepositoryFactory.ROLE);

            ArtifactRepositoryLayout layout =
                    (ArtifactRepositoryLayout) embedder.lookup(ArtifactRepositoryLayout.ROLE, "default");

            String updatePolicy = online ? ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS : ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER;
            ArtifactRepositoryPolicy snapshotsPolicy =
                    new ArtifactRepositoryPolicy(true, updatePolicy, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy(true, updatePolicy, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);

            MavenSettingsBuilder settingsBuilder = (MavenSettingsBuilder) embedder.lookup(MavenSettingsBuilder.ROLE);

            Settings settings = settingsBuilder.buildSettings();
            String localRepo = settings.getLocalRepository();

            String fileUrl = new File(localRepo).toURI().toURL().toString();
            localRepository = artifactRepositoryFactory.createArtifactRepository("local", fileUrl, layout, snapshotsPolicy, releasesPolicy);

            if (online) {
                setupRemoteRepositories(settings, artifactRepositoryFactory, layout, snapshotsPolicy, releasesPolicy);
                setupMirrors(settings, artifactRepositoryFactory, layout, snapshotsPolicy, releasesPolicy);
            }

        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }

    }

    /**
     * Read remote repository URLs from settings and create artifact repositories
     *
     * @param settings
     * @param factory
     * @param layout
     * @param snapshotsPolicy
     * @param releasesPolicy
     */
    private void setupRemoteRepositories(Settings settings,
                                         ArtifactRepositoryFactory factory,
                                         ArtifactRepositoryLayout layout,
                                         ArtifactRepositoryPolicy snapshotsPolicy,
                                         ArtifactRepositoryPolicy releasesPolicy) {

        // Read repository urls from settings file
        List<String> repositoryUrls = resolveActiveProfileRepositories(settings);
        repositoryUrls.addAll(Arrays.asList(remoteRepositoryUrls));

        for (String url : repositoryUrls) {
            ArtifactRepository repository = createArtifactRepository(url, factory, layout, snapshotsPolicy, releasesPolicy);
            remoteRepositories.add(repository);
        }
    }

    /**
     * Read mirror URLs from settings and create artifact repositories
     *
     * @param settings
     * @param factory
     * @param layout
     * @param snapshotsPolicy
     * @param releasesPolicy
     */
    private void setupMirrors(Settings settings,
                              ArtifactRepositoryFactory factory,
                              ArtifactRepositoryLayout layout,
                              ArtifactRepositoryPolicy snapshotsPolicy,
                              ArtifactRepositoryPolicy releasesPolicy) {

        List<String> mirrorUrls = resolveMirrorUrls(settings);
        for (String mirrorUrl : mirrorUrls) {
            ArtifactRepository repository = createArtifactRepository(mirrorUrl, factory, layout, snapshotsPolicy, releasesPolicy);
            remoteMirrors.add(repository);
        }

    }


    private static ArtifactRepository createArtifactRepository(String repositoryUrl,
                                                               ArtifactRepositoryFactory artifactRepositoryFactory,
                                                               ArtifactRepositoryLayout layout,
                                                               ArtifactRepositoryPolicy snapshotsPolicy,
                                                               ArtifactRepositoryPolicy releasesPolicy) {

        String id = convertUrlToRepositoryId(repositoryUrl);
        return artifactRepositoryFactory.createArtifactRepository(id, repositoryUrl, layout, snapshotsPolicy, releasesPolicy);
    }

    /**
     * Converts a repository URL into a repository id
     *
     * @param remoteRepositoryUrl a repository URL
     * @return repository id
     */
    private static String convertUrlToRepositoryId(String remoteRepositoryUrl) {
        assert remoteRepositoryUrl != null : "remoteRepositoryUrl cannot be null";
        String repoid = remoteRepositoryUrl.replace(':', '_');
        repoid = repoid.replace('/', '_');
        repoid = repoid.replace('\\', '_');
        return repoid;
    }


    /**
     * Construct a list of repositories from any active profiles
     *
     * @param settings The Maven settings to be used
     * @return List<Repository> of remote repositories in order of precedence
     */
    // Suppress Warnings for conversion from raw types 
    @SuppressWarnings("unchecked")
    private List<String> resolveActiveProfileRepositories(Settings settings) {
        List<String> repositories = new ArrayList<String>();
        Map<String, Profile> profilesMap = (Map<String, Profile>) settings.getProfilesAsMap();
        for (Object nextProfileId : settings.getActiveProfiles()) {
            Profile nextProfile = profilesMap.get((String) nextProfileId);
            if (nextProfile.getRepositories() != null) {
                for (Object repository : nextProfile.getRepositories()) {
                    String url = ((Repository) repository).getUrl();
                    repositories.add(url);
                }
            }
        }
        return repositories;
    }


    /**
     * Construct a list of mirror urls from the maven settings
     *
     * @param settings The Maven settings to be used
     * @return List<String> of mirror urls
     */
    // Suppress Warnings for conversion from raw types 
    @SuppressWarnings("unchecked")
    private List<String> resolveMirrorUrls(Settings settings) {
        List<String> mirrorUrls = new ArrayList<String>();
        List<Mirror> mirrors = (List<Mirror>) settings.getMirrors();
        for (Mirror mirror : mirrors) {
            mirrorUrls.add(mirror.getUrl());
        }
        return mirrorUrls;
    }

    /**
     * Resolves transitive dependencies.
     *
     * @param rootArtifact      the root artifact to resolve
     * @param mavenRootArtifact the root maven artifact
     * @return true if the dependencies were successfully resolved
     * @throws RepositoryException if an error is encoutered attempting to resolve a dependency
     */
    private boolean resolveDependencies(Artifact rootArtifact, org.apache.maven.artifact.Artifact mavenRootArtifact)
            throws RepositoryException {

        try {

            ResolutionGroup resolutionGroup;
            ArtifactResolutionResult result;

            resolutionGroup = metadataSource.retrieve(mavenRootArtifact, localRepository, remoteRepositories);
            Set artifacts = resolutionGroup.getArtifacts();
            result = artifactResolver.resolveTransitively(artifacts, mavenRootArtifact, remoteRepositories, localRepository, metadataSource);

            // Add the artifacts to the deployment unit
            for (Object obj : result.getArtifacts()) {
                org.apache.maven.artifact.Artifact depArtifact = (org.apache.maven.artifact.Artifact) obj;
                Artifact artifact = new Artifact();
                artifact.setName(depArtifact.getArtifactId());
                artifact.setGroup(depArtifact.getGroupId());
                artifact.setType(depArtifact.getType());
                artifact.setVersion(depArtifact.getVersion());
                artifact.setClassifier(depArtifact.getClassifier());
                artifact.setUrl(depArtifact.getFile().toURL());
                rootArtifact.addDependency(artifact);
            }

        } catch (MalformedURLException ex) {
            throw new RepositoryException(ex);
        } catch (ArtifactMetadataRetrievalException ex) {
            return false;
        } catch (ArtifactResolutionException ex) {
            return false;
        } catch (ArtifactNotFoundException ex) {
            return false;
        }

        return true;

    }

}
