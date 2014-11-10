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
package org.fabric3.api.host.contribution;

import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;

/**
 * A contribution artifact that is sourced from a filesystem.
 */
public class FileContributionSource implements ContributionSource {
    private URI uri;
    private URL location;
    private Source source;
    private long timestamp;
    private String contentType;
    private boolean extension;

    public FileContributionSource(URI uri, URL location, long timestamp, boolean extension) {
        this(uri, location, timestamp, null, extension);
    }

    public FileContributionSource(URI uri, URL location, long timestamp, String contentType, boolean extension) {
        this.uri = uri;
        this.location = location;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.extension = extension;
        this.source = new UrlSource(location);
    }

    public URI getUri() {
        return uri;
    }

    public boolean persist() {
        return false;
    }

    public boolean isExtension() {
        return extension;
    }

    public Source getSource() {
        return source;
    }

    public URL getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContentType() {
        return contentType;
    }


}


