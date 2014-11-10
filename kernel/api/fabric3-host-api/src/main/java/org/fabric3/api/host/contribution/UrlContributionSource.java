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
package org.fabric3.api.host.contribution;

import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;

/**
 * A contribution artifact that is sourced from a URL.
 */
public class UrlContributionSource implements ContributionSource {
    private URI uri;
    private URL url;
    private boolean persist;
    private Source source;

    public UrlContributionSource(URI uri, URL url, boolean persist) {
        this.uri = uri;
        this.url = url;
        this.persist = persist;
        this.source = new UrlSource(uri.toString(), url);
    }

    public URI getUri() {
        return uri;
    }

    public boolean persist() {
        return persist;
    }

    public boolean isExtension() {
        return false;
    }

    public Source getSource() {
        return source;
    }

    public URL getLocation() {
        return url;
    }

    public long getTimestamp() {
        return 0;
    }

    public String getContentType() {
        return null;
    }
}
