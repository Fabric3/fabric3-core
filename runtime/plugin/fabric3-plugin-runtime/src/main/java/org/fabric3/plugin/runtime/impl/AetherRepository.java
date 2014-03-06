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
package org.fabric3.plugin.runtime.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.fabric3.host.repository.Repository;
import org.fabric3.host.repository.RepositoryException;

/**
 * A {@link Repository} backed by an Aether repository system.
 */
public class AetherRepository implements Repository {
    private RepositorySystem repositorySystem;
    private RepositorySystemSession session;

    public AetherRepository(RepositorySystem repositorySystem, RepositorySystemSession session) {
        this.repositorySystem = repositorySystem;
        this.session = session;
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
        try {
            Artifact artifact = new DefaultArtifact(uri.toString());
            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(artifact);
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
