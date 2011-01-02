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
package org.fabric3.runtime.maven3.repository;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import org.fabric3.host.repository.Repository;
import org.fabric3.host.repository.RepositoryException;

/**
 * A Repository implementation that delegates to a set of local and remote Maven 3 repositories.
 *
 * @version $Rev$ $Date$
 */
public class Maven3Repository implements Repository {
    private static final String USER_HOME = System.getProperty("user.home");
    private static final File DEFAULT_MAVEN_REPO = new File(USER_HOME, ".m2");
    private static final File DEFAULT_USER_SETTINGS = new File(DEFAULT_MAVEN_REPO, "settings.xml");
    private static final File DEFAULT_GLOBAL_SETTINGS =
            new File(System.getProperty("maven.home", System.getProperty("user.dir", "")), "conf/settings.xml");
    private static final File DEFAULT_M2_GLOBAL_SETTINGS = new File(System.getProperty("M2_HOME"), "conf/settings.xml");

    private RepositorySystem repositorySystem;
    private MavenRepositorySystemSession session;

    public void init() throws RepositoryException {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        repositorySystem = locator.getService(RepositorySystem.class);
        session = new MavenRepositorySystemSession();

        DefaultSettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        if (DEFAULT_GLOBAL_SETTINGS.exists()) {
            request.setGlobalSettingsFile(DEFAULT_GLOBAL_SETTINGS);
        } else {
            request.setGlobalSettingsFile(DEFAULT_M2_GLOBAL_SETTINGS);
        }
        request.setUserSettingsFile(DEFAULT_USER_SETTINGS);

        try {
            SettingsBuildingResult result = builder.build(request);
            String location = result.getEffectiveSettings().getLocalRepository();
            if (location == null) {
                location = DEFAULT_MAVEN_REPO.getName();
            }
            LocalRepository localRepository = new LocalRepository(location);
            LocalRepositoryManager manager = repositorySystem.newLocalRepositoryManager(localRepository);
            session.setLocalRepositoryManager(manager);
        } catch (SettingsBuildingException e) {
            throw new RepositoryException(e);
        }
    }

    public void shutdown() throws RepositoryException {
    }

    public URL store(URI uri, InputStream stream, boolean extension) throws RepositoryException {
        return find(uri);
    }

    public boolean exists(URI uri) {
        // always return false
        return false;
    }

    public URL find(URI uri) throws RepositoryException {
        RemoteRepository central = new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
        try {
            Artifact artifact = new DefaultArtifact(uri.toString());
            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(artifact);
            request.setRepositories(Collections.singletonList(central));
            ArtifactResult result = repositorySystem.resolveArtifact(session, request);
            return result.getArtifact().getFile().toURI().toURL();
        } catch (ArtifactResolutionException e) {
            throw new RepositoryException(e);
        } catch (MalformedURLException e) {
            throw new RepositoryException(e);
        }
    }

    public void remove(URI uri) {
    }

    public List<URI> list() {
        throw new UnsupportedOperationException();
    }


}
