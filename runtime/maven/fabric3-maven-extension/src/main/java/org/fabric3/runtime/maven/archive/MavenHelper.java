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
package org.fabric3.runtime.maven.archive;

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

/**
 * Utility class for embedding Maven.
 *
 * @version $Rev$ $Date$
 */
public class MavenHelper {

    /**
     * Remote repository URLs
     */
    private final String[] remoteRepositoryUrls;

    /**
     * Maven metadata source
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * Artifact factory
     */
    private ArtifactFactory artifactFactory;

    /**
     * Local artifact repository
     */
    private ArtifactRepository localRepository;

    /**
     * Remote artifact repositories
     */
    private List<ArtifactRepository> remoteRepositories = new LinkedList<ArtifactRepository>();

    /**
     * TODO Make use of mirrors in Artifact resolution (when remote repositories are unavailable Remote artifact mirrors
     */
    private List<ArtifactRepository> remoteMirrors = new LinkedList<ArtifactRepository>();


    /**
     * Artifact resolver
     */
    private ArtifactResolver artifactResolver;

    /**
     * Online
     */
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
     * @throws Fabric3DependencyException If unable to start the embedder.
     */
    public void start() throws Fabric3DependencyException {

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
            throw new Fabric3DependencyException(ex);
        } catch (PlexusContainerException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (ComponentLookupException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (NoSuchFieldException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (IllegalAccessException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (ClassNotFoundException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (NoSuchMethodException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (InstantiationException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (InvocationTargetException ex) {
            throw new Fabric3DependencyException(ex);
        }

    }

    /**
     * Stops the embedder.
     *
     * @throws Fabric3DependencyException If unable to stop the embedder.
     */
    public void stop() throws Fabric3DependencyException {
    }

    /**
     * Resolves the dependencies transitively.
     *
     * @param rootArtifact Artifact whose dependencies need to be resolved.
     * @return true if the artifact was succesfully resolved
     * @throws Fabric3DependencyException If unable to resolve the dependencies.
     */
    public boolean resolveTransitively(Artifact rootArtifact) throws Fabric3DependencyException {

        org.apache.maven.artifact.Artifact mavenRootArtifact;
        mavenRootArtifact = artifactFactory.createArtifact(rootArtifact.getGroup(),
                                                           rootArtifact.getName(),
                                                           rootArtifact.getVersion(),
                                                           SCOPE_RUNTIME,
                                                           rootArtifact.getType());
        try {

            if (resolve(mavenRootArtifact)) {
                rootArtifact.setUrl(mavenRootArtifact.getFile().toURL());
                return resolveDependencies(rootArtifact, mavenRootArtifact);
            } else {
                return false;
            }
        } catch (MalformedURLException ex) {
            throw new Fabric3DependencyException(ex);
        }

    }

    /*
     * Resolves the artifact.
     */
    private boolean resolve(org.apache.maven.artifact.Artifact mavenRootArtifact) {

        try {
            artifactResolver.resolve(mavenRootArtifact, remoteRepositories, localRepository);
            return true;
        } catch (ArtifactResolutionException ex) {
            return false;
        } catch (ArtifactNotFoundException ex) {
            return false;
        }

    }

    /*
    * Sets up local and remote repositories.
    */
    private void setUpRepositories(Embedder embedder) {

        try {

            ArtifactRepositoryFactory artifactRepositoryFactory =
                    (ArtifactRepositoryFactory) embedder.lookup(ArtifactRepositoryFactory.ROLE);

            ArtifactRepositoryLayout layout =
                    (ArtifactRepositoryLayout) embedder.lookup(ArtifactRepositoryLayout.ROLE, "default");

            String updatePolicy =
                    online ? ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS : ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER;
            ArtifactRepositoryPolicy snapshotsPolicy =
                    new ArtifactRepositoryPolicy(true, updatePolicy, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            ArtifactRepositoryPolicy releasesPolicy =
                    new ArtifactRepositoryPolicy(true, updatePolicy, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);

            MavenSettingsBuilder settingsBuilder = (MavenSettingsBuilder) embedder.lookup(MavenSettingsBuilder.ROLE);

            Settings settings = settingsBuilder.buildSettings();
            String localRepo = settings.getLocalRepository();

            localRepository = artifactRepositoryFactory.createArtifactRepository("local",
                                                                                 new File(localRepo).toURI().toURL().toString(),
                                                                                 layout,
                                                                                 snapshotsPolicy,
                                                                                 releasesPolicy);

            if (online) {
                setupRemoteRepositories(settings, artifactRepositoryFactory, layout, snapshotsPolicy, releasesPolicy);
                setupMirrors(settings, artifactRepositoryFactory, layout, snapshotsPolicy, releasesPolicy);
            }

        } catch (Exception ex) {
            throw new Fabric3DependencyException(ex);
        }

    }

    /**
     * Read remote repository URLs from settings and create artifact repositories
     *
     * @param settings
     * @param artifactRepositoryFactory
     * @param layout
     * @param snapshotsPolicy
     * @param releasesPolicy
     */
    private void setupRemoteRepositories(
            Settings settings,
            ArtifactRepositoryFactory artifactRepositoryFactory,
            ArtifactRepositoryLayout layout,
            ArtifactRepositoryPolicy snapshotsPolicy,
            ArtifactRepositoryPolicy releasesPolicy) {

        // Read repository urls from settings file
        List<String> repositoryUrls = resolveActiveProfileRepositories(settings);
        repositoryUrls.addAll(Arrays.asList(remoteRepositoryUrls));

        for (String remoteRepositoryUrl : repositoryUrls) {
            remoteRepositories.add(
                    createArtifactRepository(
                            remoteRepositoryUrl,
                            artifactRepositoryFactory,
                            layout,
                            snapshotsPolicy,
                            releasesPolicy
                    )
            );
        }
    }

    /**
     * Read mirror URLs from settings and create artifact repositories
     *
     * @param settings
     * @param artifactRepositoryFactory
     * @param layout
     * @param snapshotsPolicy
     * @param releasesPolicy
     */
    private void setupMirrors(
            Settings settings,
            ArtifactRepositoryFactory artifactRepositoryFactory,
            ArtifactRepositoryLayout layout,
            ArtifactRepositoryPolicy snapshotsPolicy,
            ArtifactRepositoryPolicy releasesPolicy) {

        List<String> mirrorUrls = resolveMirrorUrls(settings);

        for (String mirrorUrl : mirrorUrls) {
            remoteMirrors.add(
                    createArtifactRepository(
                            mirrorUrl,
                            artifactRepositoryFactory,
                            layout,
                            snapshotsPolicy,
                            releasesPolicy
                    )
            );
        }

    }


    /**
     * Creates an ArtifactFactory
     *
     * @param repositoryUrl
     * @return
     */
    private static ArtifactRepository createArtifactRepository(
            String repositoryUrl,
            ArtifactRepositoryFactory artifactRepositoryFactory,
            ArtifactRepositoryLayout layout,
            ArtifactRepositoryPolicy snapshotsPolicy,
            ArtifactRepositoryPolicy releasesPolicy) {

        String repositoryId = convertUrlToRepositoryId(repositoryUrl);

        return
                artifactRepositoryFactory.createArtifactRepository(
                        repositoryId,
                        repositoryUrl,
                        layout,
                        snapshotsPolicy,
                        releasesPolicy);
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

    /*
    * Resolves transitive dependencies.
    */
    private boolean resolveDependencies(Artifact rootArtifact, org.apache.maven.artifact.Artifact mavenRootArtifact) {

        try {

            ResolutionGroup resolutionGroup = null;
            ArtifactResolutionResult result = null;

            resolutionGroup = metadataSource.retrieve(mavenRootArtifact, localRepository, remoteRepositories);
            result = artifactResolver.resolveTransitively(resolutionGroup.getArtifacts(),
                                                          mavenRootArtifact,
                                                          remoteRepositories,
                                                          localRepository,
                                                          metadataSource);

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

        } catch (ArtifactMetadataRetrievalException ex) {
            return false;
        } catch (MalformedURLException ex) {
            throw new Fabric3DependencyException(ex);
        } catch (ArtifactResolutionException ex) {
            return false;
        } catch (ArtifactNotFoundException ex) {
            return false;
        }

        return true;

    }

}
