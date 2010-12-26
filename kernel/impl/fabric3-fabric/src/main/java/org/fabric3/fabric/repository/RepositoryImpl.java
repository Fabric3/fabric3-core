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

import org.fabric3.host.repository.Repository;
import org.fabric3.host.repository.RepositoryException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.util.FileHelper;

/**
 * The default implementation of a Repository that persists artifacts to the file system. Extensions will be persisted to the runtime repository
 * directory (where they will not be shared by runtime instances). User contributions will be persisted to the user repository directory.
 *
 * @version $Rev$ $Date$
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
        archiveUriToUrl = new ConcurrentHashMap<URI, URL>();
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
        return new ArrayList<URI>(archiveUriToUrl.keySet());
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
