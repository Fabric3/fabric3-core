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
import org.fabric3.api.host.repository.Repository;
import org.fabric3.api.host.repository.RepositoryException;

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
        } catch (ArtifactResolutionException | MalformedURLException e) {
            throw new RepositoryException(e);
        }
    }

    public void remove(URI uri) {
    }

    public List<URI> list() {
        throw new UnsupportedOperationException();
    }

}
