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
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.repository.ArtifactCache;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class FSArtifactCache implements ArtifactCache {
    private File tempDir;
    private Map<URI, Entry> entries;

    public FSArtifactCache(@Reference HostInfo info) {
        tempDir = new File(info.getTempDir(), "cache");
        entries = new HashMap<>();
    }

    @Init
    public void init() throws IOException {
        if (tempDir.exists()) {
            FileHelper.deleteDirectory(tempDir);
        }
        tempDir.mkdirs();
    }

    public synchronized URL cache(URI uri, InputStream stream) throws ContainerException {
        if (entries.containsKey(uri)) {
            throw new ContainerException("Entry for URI already exists: " + uri);
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
            throw new ContainerException(e);
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

    public synchronized boolean remove(URI uri) {
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
        if (suffix == null) {
            return null;
        }
        return suffix.replace("/", "_");
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
