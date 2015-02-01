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
package org.fabric3.api.host.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.fabric3.api.host.util.FileHelper;

/**
 * Provides a content stream for for an artifact referenced by a URL.
 */
public class UrlSource implements Source {

    private String systemId;
    private URL url;

    public UrlSource(URL url) {
        systemId = url.toString();
        this.url = url;
    }

    public UrlSource(String systemId, URL url) {
        this.systemId = systemId;
        this.url = url;
    }


    public String getSystemId() {
        return systemId;
    }

    public URL getBaseLocation() {
        return url;
    }

    public InputStream openStream() throws IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        return connection.getInputStream();
    }
}