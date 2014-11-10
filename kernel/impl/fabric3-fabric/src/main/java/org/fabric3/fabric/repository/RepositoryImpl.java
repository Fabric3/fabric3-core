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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.host.repository.Repository;
import org.fabric3.api.host.repository.RepositoryException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;

/**
 * The default implementation of a Repository that persists artifacts to the file system. Extensions will be persisted to the runtime repository
 * directory (where they will not be shared by runtime instances). User contributions will be persisted to the user repository directory.
 */
public class RepositoryImpl implements Repository {
    private Map<URI, URL> archiveUriToUrl;
    private File runtimeDirectory;
    private File userDirectory;
    private File sharedDirectory;

    /**
     * Constructor.
     *
     * @param info the host info for the runtime
     */
    public RepositoryImpl(HostInfo info) {
        archiveUriToUrl = new ConcurrentHashMap<>();
        runtimeDirectory = info.getRuntimeRepositoryDirectory();
        sharedDirectory = info.getExtensionsRepositoryDirectory();
        userDirectory = info.getUserRepositoryDirectory();
    }

    public void init() throws RepositoryException {
        if (runtimeDirectory == null || !runtimeDirectory.exists() || !runtimeDirectory.isDirectory()) {
            return;
        }
        // load artifacts
        try {
            for (File file : sharedDirectory.listFiles()) {
                archiveUriToUrl.put(mapToUri(file), file.toURI().toURL());
            }
            for (File file : runtimeDirectory.listFiles()) {
                archiveUriToUrl.put(mapToUri(file), file.toURI().toURL());
            }
            for (File file : userDirectory.listFiles()) {
                archiveUriToUrl.put(mapToUri(file), file.toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new RepositoryException(e);
        }
    }

    public void shutdown() throws RepositoryException {

    }

    public URL store(URI uri, InputStream stream, boolean extension) throws RepositoryException {
        try {
            File location;
            if (extension) {
                location = mapToFile(runtimeDirectory, uri);
            } else {
                location = mapToFile(userDirectory, uri);
            }
            FileHelper.write(stream, location);
            URL locationUrl = location.toURI().toURL();
            archiveUriToUrl.put(uri, locationUrl);
            return locationUrl;
        } catch (IOException e) {
            String id = uri.toString();
            throw new RepositoryException("Error storing: " + id, e);
        }
    }

    public boolean exists(URI uri) {
        return archiveUriToUrl.containsKey(uri);
    }

    public URL find(URI uri) {
        return archiveUriToUrl.get(uri);
    }

    public void remove(URI uri) throws RepositoryException {
        try {
            File location = mapToFile(userDirectory, uri);
            if (!location.exists()) {
                // not a user contribution
                location = mapToFile(runtimeDirectory, uri);
            }
            archiveUriToUrl.remove(uri);
            location.delete();
        } catch (IOException e) {
            String id = uri.toString();
            throw new RepositoryException("Error removing: " + id, e);
        }
    }

    public List<URI> list() {
        return new ArrayList<>(archiveUriToUrl.keySet());
    }

    /**
     * Resolve contribution location in the repository.
     *
     * @param base the base repository directory
     * @param uri  the uri to resolve @return the mapped file
     * @return the mapped file
     * @throws IOException if an exception occurs mapping the file
     */
    private File mapToFile(File base, URI uri) throws IOException {
        if (!base.exists() || !base.isDirectory() || !base.canRead()) {
            throw new IOException("The repository location is not a directory: " + base);
        }
        return new File(base, uri.getPath());
    }

    /**
     * Maps a file to a URI.
     *
     * @param file the file
     * @return the URI
     */
    private URI mapToUri(File file) {
        return URI.create(file.getName());
    }

}
