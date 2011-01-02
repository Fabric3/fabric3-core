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
package org.fabric3.runtime.maven.repository;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.fabric3.host.repository.Repository;
import org.fabric3.host.repository.RepositoryException;

/**
 * A Repository implementation that delegates to a set of local and remote Maven repositories.
 *
 * @version $Rev$ $Date$
 */
public class MavenRepository implements Repository {
    private MavenHelper helper;

    public void init() throws RepositoryException {
        helper = new MavenHelper();
        helper.start();
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
        // assume uri is in the form 'group id:artifact id: version'
        String[] parsed = uri.toString().split(":");
        Artifact artifact = new Artifact();
        artifact.setGroup(parsed[0]);
        artifact.setName(parsed[1]);
        artifact.setVersion(parsed[2]);
        artifact.setType("jar");
        try {
            if (!helper.resolveTransitively(artifact)) {
                return null;
            }
        } catch (RepositoryException e) {
            String id = uri.toString();
            throw new RepositoryException("Error finding archive: " + id, e);
        }
        return artifact.getUrl();
    }

    public void remove(URI uri) {
    }

    public List<URI> list() {
        throw new UnsupportedOperationException();
    }


}
