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
package org.fabric3.fabric.runtime.bootstrap;

import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;

/**
 * ContributionSource for a directory that serves as a synthetic composite. For example, a datasource directory that contains JDBC drivers.
 */
public class SyntheticContributionSource implements ContributionSource {
    private static final String CONTENT_TYPE = "application/vnd.fabric3.synthetic";
    private URI uri;
    private URL location;
    private boolean extension;
    private Source source;

    public SyntheticContributionSource(URI uri, URL location, boolean extension) {
        this.uri = uri;
        this.location = location;
        this.extension = extension;
        this.source = new UrlSource(location);
    }

    public URI getUri() {
        return uri;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public boolean persist() {
        return false;
    }

    public boolean isExtension() {
        return extension;
    }

    public URL getLocation() {
        return location;
    }

    public Source getSource() {
        return source;
    }

    public long getTimestamp() {
        return 0;
    }

}
