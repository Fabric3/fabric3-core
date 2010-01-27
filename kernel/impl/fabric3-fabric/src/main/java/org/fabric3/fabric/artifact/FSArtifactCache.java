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
package org.fabric3.fabric.artifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;
import org.fabric3.util.io.FileHelper;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class FSArtifactCache implements ArtifactCache {
    private File tempDir;
    private Map<URI, Entry> entries;

    public FSArtifactCache(@Reference HostInfo info) {
        tempDir = new File(info.getTempDir(), "cache");
        entries = new HashMap<URI, Entry>();
    }

    @Init
    public void init() throws IOException {
        if (tempDir.exists()) {
            FileHelper.deleteDirectory(tempDir);
        }
        tempDir.mkdirs();
    }

    public synchronized URL cache(URI uri, InputStream stream) throws CacheException {
        if (entries.containsKey(uri)) {
            throw new CacheRuntimeException("Entry for URI already exists: " + uri);
        }
        try {
            String suffix = getSuffix(uri);
            File file = File.createTempFile("fabric3-", suffix, tempDir);
            FileHelper.write(stream, file);
            URL url = file.toURI().toURL();
            Entry entry = new Entry(url, file);
            entries.put(uri, entry);
            file.deleteOnExit();
            return url;
        } catch (IOException e) {
            throw new CacheException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized URL get(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return null;
        }
        return entry.getEntryURL();
    }

    public synchronized boolean release(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return false;
        }
        entry.getFile().delete();
        entries.remove(uri);
        return true;
    }

    /**
     * Calculates the temporary file name suffix based on the presence of a '.' in the URI path. Suffixes are used to preserve file MIME types, which
     * are often calculated from the file extension.
     *
     * @param uri the uri
     * @return the suffix or null if the URI does not contain a trailing "." in the URI path.
     */
    private String getSuffix(URI uri) {
        String suffix = null;
        String strUri = uri.toString();
        int pos = strUri.lastIndexOf(".");
        if (pos >= 0) {
            suffix = strUri.substring(pos);
        }
        return suffix;
    }

    private class Entry {
        private URL entryURL;
        private File file;

        private Entry(URL entryURL, File file) {
            this.entryURL = entryURL;
            this.file = file;
        }

        public URL getEntryURL() {
            return entryURL;
        }

        public File getFile() {
            return file;
        }
    }
}
