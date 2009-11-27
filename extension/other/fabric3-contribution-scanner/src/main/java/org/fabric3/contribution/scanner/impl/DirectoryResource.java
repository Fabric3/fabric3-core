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
package org.fabric3.contribution.scanner.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.contribution.scanner.spi.AbstractResource;
import org.fabric3.contribution.scanner.spi.FileSystemResource;

/**
 * Represents a directory that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class DirectoryResource extends AbstractResource {
    private final File root;
    // the list of resources to track changes against
    private List<FileSystemResource> resources;

    public DirectoryResource(File root) {
        this.root = root;
        resources = new ArrayList<FileSystemResource>();
    }

    public String getName() {
        return root.getName();
    }

    public URL getLocation() {
        try {
            return root.toURI().normalize().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public long getTimestamp() {
        return root.lastModified();
    }

    public void addResource(FileSystemResource resource) {
        resources.add(resource);
    }

    public void reset() throws IOException {
        for (FileSystemResource resource : resources) {
            resource.reset();
        }
        checksumValue = checksum();
    }

    protected byte[] checksum() {
        try {
            MessageDigest checksum = MessageDigest.getInstance("MD5");
            for (FileSystemResource resource : resources) {
                checksum.update(resource.getChecksum());
            }
            return checksum.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

}
